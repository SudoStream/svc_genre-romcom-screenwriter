package io.sudostream.api_antagonist.screenwriter.romcom.business

import java.util.UUID

import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import io.sudostream.api_antagonist.messages._
import org.scalatest.FunSuite

class RomcomGenerator$Test extends FunSuite {

  def createGreenlitFilm: GreenlitFilm = {
    val topLevelGetHttpQuestion = HttpQuestionForTheProtagonistAPI("/stars", HttpMethod.GET, List(200, 404, 503))
    val topLevelDeleteHttpQuestion = HttpQuestionForTheProtagonistAPI("/stars", HttpMethod.DELETE, List(200))
    val topLevelPutHttpQuestion = HttpQuestionForTheProtagonistAPI("/stars", HttpMethod.DELETE, List(200))
    val topLevelPostHttpQuestion = HttpQuestionForTheProtagonistAPI("/stars", HttpMethod.DELETE, List(200))

    val speculativeScreenplay =
      SpeculativeScreenplay(
        "apiTitle",
        Some("apiDesc"),
        Some("v1"),
        "hostname.com",
        Some("api"),
        List("http"),
        List(80),
        List(topLevelGetHttpQuestion, topLevelDeleteHttpQuestion, topLevelPostHttpQuestion, topLevelPutHttpQuestion)
      )

    GreenlitFilm("Stars Film", FilmGenre.ROMANCE, UUID.randomUUID().toString, speculativeScreenplay)
  }

  test("testCreateRomcomScript") {

    val greenlitFilm = createGreenlitFilm
    val romcomFinalScript = RomcomGenerator.createRomcomScript(greenlitFilm)

    println("testCreateRomcomScript - start")
    println(romcomFinalScript.toString)

    assert(romcomFinalScript.filmUuid == greenlitFilm.filmUuid)
    assert(romcomFinalScript.antagonistLines.size == 1)
    assert(romcomFinalScript.antagonistLines.head.method == HttpMethod.GET)
    assert(romcomFinalScript.antagonistLines.head.documentedReturnCodes.size == 3)
    assert(romcomFinalScript.antagonistLines.head.documentedReturnCodes.contains(200))
    assert(romcomFinalScript.antagonistLines.head.documentedReturnCodes.contains(404))
    assert(romcomFinalScript.antagonistLines.head.documentedReturnCodes.contains(503))
    assert(romcomFinalScript.antagonistLines.head.uriPath == "http://hostname.com:80/v1/stars")
    assert(romcomFinalScript.antagonistLines.head.entity == "")

    println("testCreateRomcomScript - start")
  }

  test("test that we can create an HttpRequest") {
    val romcomFinalScript = RomcomGenerator.createRomcomScript(createGreenlitFilm)

    println("HttpRequests...")
    for (request <- romcomFinalScript.antagonistLines) {
        val httpReq = HttpRequest(
          convertHttpMethod(request.method),
          uri = request.uriPath)
      println(httpReq.toString())
    }
  }

  def convertHttpMethod(internalHttpMethod: HttpMethod) : akka.http.scaladsl.model.HttpMethod = {
    internalHttpMethod match {
      case HttpMethod.GET => HttpMethods.GET
      case HttpMethod.POST => HttpMethods.POST
      case HttpMethod.PUT => HttpMethods.PUT
      case HttpMethod.DELETE => HttpMethods.DELETE
      case _ => HttpMethods.GET
    }
  }

}
