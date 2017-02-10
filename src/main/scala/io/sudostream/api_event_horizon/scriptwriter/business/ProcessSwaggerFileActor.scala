package io.sudostream.api_event_horizon.scriptwriter.business

import akka.actor.Actor
import io.sudostream.api_event_horizon.scram.api.SwaggerJsonApiToScreenplayWriterConverter

class ProcessSwaggerFileActor extends Actor {

  override def receive: Receive = {
    case swaggerSpec: String =>
      val starBirthScram = new SwaggerJsonApiToScreenplayWriterConverter().passApiDefinitionToScreenplayWriterAmateur(swaggerSpec).get
      sender ! starBirthScram.generateHappyPathTests
  }

}
