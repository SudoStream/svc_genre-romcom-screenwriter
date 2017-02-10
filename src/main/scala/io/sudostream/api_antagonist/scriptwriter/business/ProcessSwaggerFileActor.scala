package io.sudostream.api_antagonist.scriptwriter.business

import akka.actor.Actor
import io.sudostream.api_antagonist.screenplay_guild.api.SwaggerJsonApiToScreenplayWriterConverter

class ProcessSwaggerFileActor extends Actor {

  override def receive: Receive = {
    case swaggerSpec: String =>
      val starBirthScram = new SwaggerJsonApiToScreenplayWriterConverter().passApiDefinitionToScreenplayWriterAmateur(swaggerSpec).get
      sender ! starBirthScram.generateHappyPathTests
  }

}
