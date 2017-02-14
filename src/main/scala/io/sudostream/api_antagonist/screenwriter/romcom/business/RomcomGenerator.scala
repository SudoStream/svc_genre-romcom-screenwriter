package io.sudostream.api_antagonist.screenwriter.romcom.business

import java.util.UUID

import io.sudostream.api_antagonist.messages._

import scala.annotation.tailrec

object RomcomGenerator {

  def createRomcomScript(greenlitFilm: GreenlitFilm): FinalScript = {

    val antagonistLines = createAntagonistLines(greenlitFilm)

    FinalScript(
      s"FinalScript : ${greenlitFilm.filmTitle}",
      greenlitFilm.filmUuid,
      UUID.randomUUID().toString,
      antagonistLines
    )

  }

  private def createAntagonistLines(greenlitFilm: GreenlitFilm): List[HttpRequest] = {
    val httpQuestions = greenlitFilm.speculativeScreenplay.theAntagonistInterrogation

    @tailrec
    def extractHttpRequests(remainingHttpQuestions: List[HttpQuestionForTheProtagonistAPI],
                            currentHttpRequests: List[HttpRequest]): List[HttpRequest] = {
      if (remainingHttpQuestions.isEmpty) currentHttpRequests
      else {
        val httpQuestion = remainingHttpQuestions.head

        val newHttpRequest: Option[HttpRequest] = if (httpQuestion.method == HttpMethod.GET) // filter only GETs for now
        {
          val screenplay = greenlitFilm.speculativeScreenplay
          Some(HttpRequest(
            method = httpQuestion.method,

            uriPath =
              s"${screenplay.schemes.head}://${screenplay.hostname}:${screenplay.ports.head}/${screenplay.apiVersion.getOrElse("")}/${httpQuestion.uriPath.replace("/","")}",

            httpHeaders = Nil,
            entity = "",
            documentedReturnCodes = httpQuestion.documentedReturnCodes)
          )
        } else None

        val newHttpRequests = newHttpRequest match {
          case Some(newRequest) => newRequest :: currentHttpRequests
          case None => currentHttpRequests
        }

        extractHttpRequests(remainingHttpQuestions.tail, newHttpRequests)
      }
    }

    val httpRequests = extractHttpRequests(httpQuestions, List())
    httpRequests.reverse
  }

}
