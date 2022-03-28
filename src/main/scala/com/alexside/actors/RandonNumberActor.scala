package com.alexside.actors

import akka.actor.typed.{Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.alexside.model.GameModel

import scala.util.Random

object RandonNumberActor {

  def apply(): Behavior[GameModel.Command] =
    Behaviors.setup(context => new RandonNumberActor(context))

  class RandonNumberActor(context: ActorContext[GameModel.Command], fromBound: Int = 0, toBound: Int = 999999)
    extends AbstractBehavior[GameModel.Command](context) {

    println("RandonNumberActor actor has started")

    override def onMessage(msg: GameModel.Command): Behavior[GameModel.Command] = {
      msg match {
        case GameModel.RequestGenerateNumber(requestId, replyTo) =>
          val random = Random.between(fromBound, toBound + 1)
          println(s"Send value=$random by requestId=$requestId")
          replyTo ! GameModel.ResponseNumber(requestId, random)
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
