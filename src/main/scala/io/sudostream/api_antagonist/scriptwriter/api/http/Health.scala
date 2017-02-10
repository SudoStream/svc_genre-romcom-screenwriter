package io.sudostream.api_antagonist.scriptwriter.api.http

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer

import scala.concurrent.ExecutionContextExecutor

trait Health {
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  val health : Route = path("health") {
    get {
      complete {
        HttpResponse(StatusCodes.OK, entity = "Don't worry, she'll hold together... You hear me, baby? Hold together!\n")
      }
    }
  }

}
