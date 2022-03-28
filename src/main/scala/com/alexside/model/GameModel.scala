package com.alexside.model

import akka.actor.typed.ActorRef

object GameModel {

  final case class GamePlayer(player: String, number: Int)
  final case class GameResult(position: Int, player: String, number: Int, result: Int)

  sealed trait Command
  final case class RequestLoadGame(players: Int) extends Command

  final case class RequestRunGame(players: Seq[GamePlayer], replyTo: ActorRef[ResponseRunGame]) extends Command
  final case class ResponseRunGame(results: Seq[GameResult]) extends Command


  final case class RequestInitPlayers(players: Int, replyTo: ActorRef[ResponseInitPlayers]) extends Command
  final case class ResponseInitPlayers(players: Seq[GamePlayer]) extends Command

  final case class RequestGenerateNumber(requestId: Int, replyTo: ActorRef[ResponseNumber]) extends Command
  final case class ResponseNumber(requestId: Int, number: Int) extends Command

  def calculateScore(players: Seq[GameModel.GamePlayer]): Seq[GameModel.GameResult] = {
    players
      .map(p => {
        val score = p.number
          .toString
          .toVector
          .groupBy(ch => Integer.parseInt(ch.toString))
          .view
          .mapValues(_.size)
          .foldLeft(0)((acc, row) => {
            acc + math.pow(10, row._2-1).toInt * row._1
          })
        GameModel.GameResult(
          position = 0, player = p.player, number = p.number, result = score)
      })
      .sortBy(_.result)(Ordering[Int].reverse)
      .zipWithIndex
      .map(t => t._1.copy(position = t._2 + 1))
  }
}
