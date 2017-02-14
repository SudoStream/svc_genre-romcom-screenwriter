package io.sudostream.api_antagonist.screenwriter.romcom

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.kafka.{ConsumerSettings, ProducerSettings}
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.{Config, ConfigFactory}
import io.sudostream.api_antagonist.kafka.serialising.{FinalScriptSerializer, GreenlitFilmDeserialiser}
import io.sudostream.api_antagonist.screenwriter.romcom.api.http.ProcessApiDefinition
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer}

import scala.concurrent.ExecutionContextExecutor

object RomcomScreenwriter extends App with Service
  with ProcessApiDefinition
  with io.sudostream.api_antagonist.screenwriter.romcom.api.kafka.ProcessApiDefinition {

  override val config = ConfigFactory.load()
  override implicit val system = ActorSystem("screenwriter-system", config)
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()
  override val logger = Logging(system, getClass)

  override val kafkaConsumerBootServers = config.getString("akka.kafka.consumer.bootstrapservers")
  override val kafkaProducerBootServers = config.getString("akka.kafka.producer.bootstrapservers")

  override val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new GreenlitFilmDeserialiser)
    .withBootstrapServers(kafkaConsumerBootServers)
    .withGroupId(config.getString("akka.kafka.consumer.groupid"))
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")

  override val producerSettings = ProducerSettings(system, new ByteArraySerializer, new FinalScriptSerializer)
    .withBootstrapServers(kafkaProducerBootServers)

  publishStuffToKafka()

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))

}

trait Service {

  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer
  val logger: LoggingAdapter

  def config: Config
}
