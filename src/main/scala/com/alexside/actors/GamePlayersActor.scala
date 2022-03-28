package com.alexside.actors

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.alexside.model.GameModel
import com.alexside.model.GameModel.ResponseInitPlayers

import scala.collection.mutable.ArrayBuffer

object GamePlayersActor {

  def apply(): Behavior[GameModel.Command] =
    Behaviors.setup(context => new GamePlayersActor(context))

  class GamePlayersActor(context: ActorContext[GameModel.Command])
    extends AbstractBehavior[GameModel.Command](context) {

    var initPlayersReplyTo: Option[ActorRef[ResponseInitPlayers]] = None
    var players: Option[Int] = None
    var initPlayersBuffer = ArrayBuffer[GameModel.GamePlayer]()
    val randomNumberActor = context.spawn(RandonNumberActor(), "RandomNumberActor")

    override def onMessage(msg: GameModel.Command): Behavior[GameModel.Command] = {
      msg match {
        case GameModel.RequestInitPlayers(players, replyTo) =>
          println(s"RequestInitPlayers: $players")
          initPlayersReplyTo = Some(replyTo)
          this.players = Some(players)
          for (num <- 1 to  players) {
            randomNumberActor ! GameModel.RequestGenerateNumber(num, context.self)
          }
          this
        case GameModel.ResponseNumber(requestId, number) =>
          println(s"ResponseNumber: requestId = $requestId, number = $number")
          initPlayersBuffer += GameModel.GamePlayer(requestId.toString, number)
          if (initPlayersBuffer.size >= this.players.get) {
            initPlayersReplyTo.get ! GameModel.ResponseInitPlayers(initPlayersBuffer.toSeq)
            Behaviors.stopped
          } else this
      }
    }

    override def onSignal: PartialFunction[Signal, Behavior[GameModel.Command]] = {
      case PostStop =>
        println("InitPlayersActor actor was stopped")
        this
    }
  }
}
