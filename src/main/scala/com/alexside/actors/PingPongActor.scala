package com.alexside.actors

import akka.actor.Actor
import com.alexside.model.ServerModel.{RequestPing, ResponsePong}

import java.time.Instant

object PingPongActor {

  case object Ack
  case object StreamInitialized
  case object StreamCompleted
  final case class StreamFailure(ex: Throwable)

  class PingPongActor() extends Actor {
    import PingPongActor._

    def receive: Receive = {
      case StreamInitialized =>
        println("Stream initialized")
        sender() ! Ack // ack to allow the stream to proceed sending more elements

      case msg: RequestPing =>
        println(s"RequestPing: $msg")
        sender() ! ResponsePong(
          request_id = msg.id,
          request_at = msg.timestamp,
          timestamp = Instant.now().toEpochMilli
        ) // ack to allow the stream to proceed sending more elements

      case StreamCompleted =>
        println(s"Stream completed")
      case StreamFailure(ex) =>
        println(s"Stream failed! $ex")
    }
  }
}
