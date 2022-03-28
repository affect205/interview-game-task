package com.alexside

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.actor.TypedActor.dispatcher
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Directives.{handleWebSocketMessages, path}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.alexside.actors.PingPongActor
import com.alexside.actors.PingPongActor.PingPongActor
import com.alexside.model.ServerModel
import com.alexside.model.ServerModel._
import io.circe._
import io.circe.parser._

import scala.io.StdIn

object GameBootstrap extends App {

  val INVALID_MESSAGE = "Got invalid JSON message!"

//  println("Game bootstrap started")
//  val gameMainActor: ActorSystem[GameModel.Command] = ActorSystem(GameMainActor(), "GameMainActor")
//  gameMainActor ! GameModel.RequestLoadGame(players = 5)

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val gameWebSocketService = {
  Flow[Message]
    .collect {
      case tm: TextMessage =>

        parse(tm.getStrictText) match {
          case Left(_) =>
            TextMessage(Source.single(INVALID_MESSAGE))
          case Right(json) =>

            val message = parseAPIMessage(json)

            message match {
              case Some(x: RequestPlay) =>
                TextMessage(Source.single("Ok, let's get play"))

              case Some(request: RequestPing) =>

                val AckMessage = PingPongActor.Ack

                // sent from stream to actor to indicate start, end or failure of stream:
                val InitMessage = PingPongActor.StreamInitialized
                val OnCompleteMessage = PingPongActor.StreamCompleted
                val onErrorMessage = (ex: Throwable) => PingPongActor.StreamFailure(ex)

                val pingPongActor = system.actorOf(Props(new PingPongActor()))
                val sink: Sink[Message, Any] = Sink.actorRefWithBackpressure(
                  pingPongActor,
                  onInitMessage = InitMessage,
                  ackMessage = AckMessage,
                  onCompleteMessage = OnCompleteMessage,
                  onFailureMessage = onErrorMessage)

                TextMessage(Source.single("Ok, let's get ping pong"))

              case _ =>
                TextMessage(Source.single(INVALID_MESSAGE))
            }

          //TextMessage(Source.single("Hello ") ++ tm.textStream)
        }
      // ignore binary messages
      // TODO #20096 in case a Streamed message comes in, we should runWith(Sink.ignore) its data
    }
  }

  val route =
    path("v1") {
      Directives.get {
        handleWebSocketMessages(gameWebSocketService)
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8088)

  println(s"Server online at http://localhost:8088/api\nPress RETURN to stop...")
  StdIn.readLine() // for the future transformations
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done


  def parseAPIMessage(json: Json): Option[APIMessage] = {
    import com.alexside.model.ServerModel.Implicits._
    val cursor: HCursor = json.hcursor
    cursor.get[String](ServerModel.TAG_FIELD) match {
      case Right(ServerModel.REQUEST_PLAY_TAG) => json.as[RequestPlay].toOption
      case Right(ServerModel.REQUEST_PING_TAG) => json.as[RequestPing].toOption
      case Left(_) => None
      case _ => None
    }
  }
}
