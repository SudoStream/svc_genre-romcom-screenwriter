package io.sudostream.api_event_horizon.scriptwriter

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.kafka.{ConsumerSettings, ProducerSettings}
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.{Config, ConfigFactory}
import io.sudostream.api_event_horizon.kafka.serialising.GeneratedTestsEventSerializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer}

import scala.concurrent.ExecutionContextExecutor

object ScriptWriter extends App with Service
  with io.sudostream.api_event_horizon.scriptwriter.api.http.ProcessApiDefinition
  with io.sudostream.api_event_horizon.scriptwriter.api.kafka.ProcessApiDefinition {

  override val config = ConfigFactory.load()
  override implicit val system = ActorSystem("scriptwriter-system", config)
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()
  override val logger = Logging(system, getClass)

  override val kafkaConsumerBootServers = config.getString("akka.kafka.consumer.bootstrapservers")
  override val kafkaProducerBootServers = config.getString("akka.kafka.producer.bootstrapservers")

  override val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers(kafkaConsumerBootServers)
    .withGroupId(config.getString("akka.kafka.consumer.groupid"))
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")

  // TODO: Make acks config configurable
  override val producerSettings = ProducerSettings(system, new ByteArraySerializer, new GeneratedTestsEventSerializer)
    .withBootstrapServers(kafkaProducerBootServers)
//    .withParallelism(config.getInt("akka.kafka.producer.parallelism"))
//    .withDispatcher(config.getString("akka.kafka.producer.use-dispatcher"))
//    .withProperty(ProducerConfig.ACKS_CONFIG, config.getString("akka.kafka.producer.acks"))
//    .withProperty(ProducerConfig.BATCH_SIZE_CONFIG, config.getString("akka.kafka.producer.batch-size"))

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
