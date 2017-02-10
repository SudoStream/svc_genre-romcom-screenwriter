package io.sudostream.api_event_horizon.scriptwriter.api.kafka

import java.io.{PrintWriter, StringWriter}

import akka.Done
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import io.sudostream.api_event_horizon.messages.SpeculativeScreenPlay
import io.sudostream.api_event_horizon.scram.api.SwaggerJsonScramConverter
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.{ExecutionContextExecutor, Future}

trait ProcessApiDefinition {

  implicit def executor: ExecutionContextExecutor

  implicit val system: ActorSystem
  implicit val materializer: Materializer

  def kafkaConsumerBootServers: String
  def kafkaProducerBootServers: String

  def consumerSettings: ConsumerSettings[Array[Byte], String]

  def producerSettings: ProducerSettings[Array[Byte], SpeculativeScreenPlay]

  def logger: LoggingAdapter

  def publishStuffToKafka(): Future[Done] = {
    Consumer.committableSource(consumerSettings, Subscriptions.topics("aeh-api-definitions"))
      .map {
        msg =>
          val generatedTestsEvent = new SwaggerJsonScramConverter().convertToScram(msg.record.value()).get.generateHappyPathTests

          val msgToCommit = ProducerMessage.Message(
            new ProducerRecord[Array[Byte], SpeculativeScreenPlay]("generated-test-script", generatedTestsEvent),
            msg.committableOffset)
          println("generated tests: " + generatedTestsEvent)
          msgToCommit
      }
      .runWith(Producer.commitableSink(producerSettings))
  }

  def publishSingleEventToKafka(generatedTestsEvent: SpeculativeScreenPlay): Future[Done] = {

    val done = Source.single(generatedTestsEvent)
      .map { msg =>
        new ProducerRecord[Array[Byte], SpeculativeScreenPlay]("generated-test-script", msg)
      }
      .runWith(Producer.plainSink(producerSettings))
    logger.info("Message on its way to Kafka")
    logger.debug("Message sent:- " + generatedTestsEvent)

    done.onSuccess {
      case whoKnows => logger.info("All goood :" + whoKnows)
    }

    done.onFailure {
      case e: Throwable =>
        logger.error("Ooooh dear :- " + e.getMessage + "\n")
        val sw = new StringWriter
        e.printStackTrace(new PrintWriter(sw))
        logger.error(sw.toString)
      case somethingElse => logger.error("Not sure what happened! " + somethingElse)
    }

    done
  }

}
