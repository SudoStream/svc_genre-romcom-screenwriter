package io.sudostream.api_event_horizon.scriptwriter.api.kafka

import akka.event.{Logging, LoggingAdapter}
import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.event.EventStream
import io.sudostream.api_event_horizon.messages.GeneratedTestsEvent
import io.sudostream.api_event_horizon.scram.api.SwaggerJsonScramConverter
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.{ExecutionContextExecutor, Future}

trait ProcessApiDefinition {

  implicit def executor: ExecutionContextExecutor
  implicit val system: ActorSystem
  implicit val materializer: Materializer

  def kafkaBootServers: String

  def consumerSettings: ConsumerSettings[Array[Byte], String]

  def producerSettings: ProducerSettings[Array[Byte], GeneratedTestsEvent]

  def logger : LoggingAdapter

  def publishStuffToKafka(): Future[Done] = {
    Consumer.committableSource(consumerSettings, Subscriptions.topics("aeh-api-definitions"))
      .map {
        msg =>
          val generatedTestsEvent = new SwaggerJsonScramConverter().convertToScram(msg.record.value()).get.generateHappyPathTests

          val msgToCommit = ProducerMessage.Message(
            new ProducerRecord[Array[Byte], GeneratedTestsEvent]("generated-test-scripts", generatedTestsEvent),
            msg.committableOffset)
          println("generated tests: " + generatedTestsEvent)
          msgToCommit
      }
      .runWith(Producer.commitableSink(producerSettings))
  }

  def publishSingleEventToKafka(generatedTestsEvent: GeneratedTestsEvent) = {
    logger.info("Sent message to Kafka")
    logger.debug("Message sent:- " + generatedTestsEvent)
    Source.single(generatedTestsEvent)
      .map { msg =>
        new ProducerRecord[Array[Byte], GeneratedTestsEvent]("generated-test-scripts", msg)
      }
      .runWith(Producer.plainSink(producerSettings))
     
  }

}
