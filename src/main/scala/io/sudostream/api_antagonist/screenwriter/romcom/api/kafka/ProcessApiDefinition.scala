package io.sudostream.api_antagonist.screenwriter.romcom.api.kafka

import akka.Done
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.stream.Materializer
import io.sudostream.api_antagonist.messages.{FinalScript, GreenlitFilm}
import io.sudostream.api_antagonist.screenwriter.romcom.business.RomcomGenerator
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.{ExecutionContextExecutor, Future}

trait ProcessApiDefinition {

  implicit def executor: ExecutionContextExecutor

  implicit val system: ActorSystem
  implicit val materializer: Materializer

  def kafkaConsumerBootServers: String
  def kafkaProducerBootServers: String

  def consumerSettings: ConsumerSettings[Array[Byte], GreenlitFilm]

  def producerSettings: ProducerSettings[Array[Byte], FinalScript]

  def logger: LoggingAdapter

  def publishStuffToKafka(): Future[Done] = {
    Consumer.committableSource(consumerSettings, Subscriptions.topics("greenlit-film"))
      .map {
        msg =>
          val greenlitFilm = msg.record.value()
          val finalScript = RomcomGenerator.createRomcomScript(greenlitFilm)

          val msgToCommit = ProducerMessage.Message(
            new ProducerRecord[Array[Byte], FinalScript]("final-script", finalScript),
            msg.committableOffset)

          println("Final Script : " + finalScript)

          msgToCommit
      }
      .runWith(Producer.commitableSink(producerSettings))
  }


}
