package io.sudostream.api_event_horizon.scriptwriter

import akka.{Done, NotUsed}
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.{Config, ConfigFactory}
import io.sudostream.api_event_horizon.messages.GeneratedTestsEvent
import io.sudostream.api_event_horizon.scram.api.SwaggerJsonScramConverter
import io.sudostream.api_event_horizon.scriptwriter.api.http.ProcessApiDefinition
import io.sudostream.api_event_horizon.scriptwriter.api.kafka.GeneratedTestsEventSerializer
import io.sudostream.api_event_horizon.scriptwriter.business.ProcessSwaggerFileActor
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer}

import scala.concurrent.{ExecutionContextExecutor, Future}

trait Service extends ProcessApiDefinition {

  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer
  val logger: LoggingAdapter

  def config: Config
}

object ScriptWriter extends App with Service
  with io.sudostream.api_event_horizon.scriptwriter.api.kafka.ProcessApiDefinition {

  override val config = ConfigFactory.load()
  override implicit val system = ActorSystem("scriptwriter-system", config)
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()
  override val logger = Logging(system, getClass)

  val kafkaBootServers = config.getString("akka.kafka.consumer.bootstrapservers")

  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers(kafkaBootServers)
    .withGroupId("akka.kafka.consumer.groupid")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")

  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new GeneratedTestsEventSerializer)
    .withBootstrapServers("localhost:9092")

  val swaggerSpecActor: ActorRef = system.actorOf(Props[ProcessSwaggerFileActor])

  val done =
    Consumer.committableSource(consumerSettings, Subscriptions.topics("aeh-api-definitions"))
      .map {
        msg =>
          val generatedTestsEvent = new SwaggerJsonScramConverter().convertToScram(msg.record.value()).get.generateHappyPathTests
          println("Generated Tests := " + generatedTestsEvent)
          val msgToCommit = ProducerMessage.Message(
            new ProducerRecord[Array[Byte], GeneratedTestsEvent]("generated-test-scripts", generatedTestsEvent),
            msg.committableOffset)
          println("hello: " + generatedTestsEvent)
          msgToCommit
      }
      .runWith(Producer.commitableSink(producerSettings))


  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}
