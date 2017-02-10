package io.sudostream.api_event_horizon.scriptwriter.api.http

import java.io.{PrintWriter, StringWriter}

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.{HttpResponse, Multipart, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.Materializer
import akka.stream.scaladsl.StreamConverters
import akka.util.Timeout
import io.sudostream.api_event_horizon.messages.SpeculativeScreenPlay
import io.sudostream.api_event_horizon.scriptwriter.business.ProcessSwaggerFileActor

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

trait ProcessApiDefinition extends Health
  with io.sudostream.api_event_horizon.scriptwriter.api.kafka.ProcessApiDefinition {

  implicit def executor: ExecutionContextExecutor

  implicit val system: ActorSystem
  implicit val materializer: Materializer
  implicit val timeout = Timeout(30.seconds)

  val routes: Route = path("scriptwriter" / "apidefinition") {
    (post & entity(as[Multipart.FormData])) { fileData =>
      complete {
        processFile(fileData).map { testsGenerated =>
          publishSingleEventToKafka(testsGenerated)
          HttpResponse(StatusCodes.OK, entity = "\n" + testsGenerated + "\n\n")
        }.recover {
          case ex: Exception =>
            val stacktrace = new StringWriter
            ex.printStackTrace(new PrintWriter(stacktrace))
            HttpResponse(StatusCodes.InternalServerError, entity = "Error in file uploading\n"
              + stacktrace.toString + "\n\n")
        }
      }
    }
  } ~ health

  private def processFile(fileData: Multipart.FormData): Future[SpeculativeScreenPlay] = {
    val swaggerSpecActor: ActorRef = system.actorOf(Props[ProcessSwaggerFileActor])

    val swaggerDefinitionFuture: Future[String] = fileData.parts.mapAsync(1) { bodyPart ⇒
      val inputStream = bodyPart.entity.dataBytes.runWith(StreamConverters.asInputStream())
      val swaggerSpec = scala.io.Source.fromInputStream(inputStream).getLines
      Future {
        swaggerSpec.mkString
      }
    }.runFold("")(_ + _)

    swaggerDefinitionFuture flatMap {
      swaggerDefinition => (swaggerSpecActor ? swaggerDefinition).asInstanceOf[Future[SpeculativeScreenPlay]]
    }
  }

}
