package io.sudostream.api_event_horizon.scriptwriter.api.kafka

trait ProcessApiDefinition {

  //  implicit val system: ActorSystem
  //  implicit val materializer: Materializer

//  def publishStuffToKafka(apiDefinition: String): Future[Done] = {
//    val swaggerSpecActor: ActorRef = system.actorOf(Props[ProcessSwaggerFileActor])
//
//    val value: Source[String, NotUsed] = Source(apiDefinition)
//    val done: Future[Done] = value
//      .map {
//        apiDef =>
//          //    (swaggerSpecActor ? apiDef).asInstanceOf[Future[GeneratedTestsEvent]]
//          new SwaggerJsonScramConverter().convertToScram(apiDef).get.generateHappyPathTests
//      }
//      .map {
//          thing =>
//            new ProducerRecord[Array[Byte], GeneratedTestsEvent]("generated-test-scripts", thing)
//      }
//      .runWith(Producer.plainSink(producerSettings))
//
//    done
//  }
}
