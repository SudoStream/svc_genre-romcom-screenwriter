package io.sudostream.api_event_horizon.scriptwriter.business

import akka.actor.Actor
import io.sudostream.api_event_horizon.scram.api.SwaggerJsonScramConverter

/**
  * Created by andy on 31/08/16.
  */
class ProcessSwaggerFileActor extends Actor {

  override def receive: Receive = {
    case swaggerSpec: String =>
      val starBirthScram = new SwaggerJsonScramConverter().convertToScram(swaggerSpec).get
      sender ! starBirthScram.generateHappyPathTests
  }

}
