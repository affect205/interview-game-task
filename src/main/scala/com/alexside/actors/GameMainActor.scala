package com.alexside.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.alexside.model.GameModel

object GameMainActor {

  def apply(): Behavior[GameModel.Command] = {
    Behaviors.setup(context => new GameMainActor(context))
  }

  class GameMainActor(context: ActorContext[GameModel.Command]) extends AbstractBehavior[GameModel.Command](context) {
    override def onMessage(msg: GameModel.Command): Behavior[GameModel.Command] =
      msg match {
        case GameModel.RequestLoadGame(players) =>
          val initPlayersActor = context.spawn(GamePlayersActor(), "GamePlayersActor")
          initPlayersActor ! GameModel.RequestInitPlayers(players, context.self)
          this
        case GameModel.ResponseInitPlayers(players) =>
          println(s"ResponseInitPlayers: ${players.mkString(",")}")
          val gameRunnerActor = context.spawn(GameRunnerActor(), "GameRunnerActor")
          gameRunnerActor ! GameModel.RequestRunGame(players, context.self)
          this
        case GameModel.ResponseRunGame(results) =>
          println(s"ResponseRunGame:\n${results.mkString("\n")}")
          Behaviors.stopped
      }
  }
}
