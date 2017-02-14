package io.sudostream.api_antagonist.screenwriter.romcom.api.http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, Multipart, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

trait ProcessApiDefinition extends Health
  with io.sudostream.api_antagonist.screenwriter.romcom.api.kafka.ProcessApiDefinition {

  implicit def executor: ExecutionContextExecutor

  implicit val system: ActorSystem
  implicit val materializer: Materializer
  implicit val timeout = Timeout(30.seconds)

  val routes: Route = path("romcom-scriptwriter" / "greenlit-film") {
    (post & entity(as[Multipart.FormData])) { fileData =>
      complete {
        HttpResponse(StatusCodes.OK, entity = "\nTODO: Add test romcom screenwriter HTTP API\n\n")
      }
    }
  } ~ health

}
