package io.sudostream.api_event_horizon.scriptwriter.business

import akka.actor.Actor
import io.sudostream.api_event_horizon.scram.api.SwaggerJsonScreenplayWriterConverter

class ProcessSwaggerFileActor extends Actor {

  override def receive: Receive = {
    case swaggerSpec: String =>
      val starBirthScram = new SwaggerJsonScreenplayWriterConverter().convertToScreenplayWriterAmateur(swaggerSpec).get
      sender ! starBirthScram.generateHappyPathTests
  }

}
