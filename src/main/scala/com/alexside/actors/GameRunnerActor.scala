package com.alexside.actors

import akka.actor.typed.{Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.alexside.model.GameModel

object GameRunnerActor {

  def apply(): Behavior[GameModel.Command] =
    Behaviors.setup(context => new GameRunnerActor(context))

  class GameRunnerActor(context: ActorContext[GameModel.Command])
    extends AbstractBehavior[GameModel.Command](context) {

    println("GameRunnerActor actor has started")

    override def onMessage(msg: GameModel.Command): Behavior[GameModel.Command] = {
      msg match {
        case GameModel.RequestRunGame(players, replyTo) =>
          println(s"RequestRunGame:\n${players.mkString("\n")}")
          val result = GameModel.calculateScore(players)
          replyTo ! GameModel.ResponseRunGame(result)
          this
      }
    }

    override def onSignal: PartialFunction[Signal, Behavior[GameModel.Command]] = {
      case PostStop =>
        println("RandonNumberActor actor was stopped")
        this
    }
  }
}
