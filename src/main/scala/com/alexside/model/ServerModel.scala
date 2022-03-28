package com.alexside.model

object ServerModel {
  val TAG_FIELD = "message_type"
  val REQUEST_PLAY_TAG = "request.play"
  val REQUEST_PING_TAG = "request.ping"

  sealed trait APIMessage {
    def getMessageType: String
  }

  final case class RequestPlay(message_type: String = REQUEST_PLAY_TAG, players: Int) extends APIMessage {
    override def getMessageType: String = message_type
  }
  final case class ResponsePlay(message_type: String = "response.results", result: Seq[GameModel.GameResult]) extends APIMessage {
    override def getMessageType: String = message_type
  }
  final case class RequestPing(message_type: String = REQUEST_PING_TAG, id: String, timestamp: Long) extends APIMessage {
    override def getMessageType: String = message_type
  }
  final case class ResponsePong(message_type: String = "response.pong", request_id: String, request_at: Long, timestamp: Long) extends APIMessage {
    override def getMessageType: String = message_type
  }

  object Implicits {
    import io.circe._
    import io.circe.generic.semiauto._
    implicit val requestPlayDecoder: Decoder[RequestPlay] = deriveDecoder[RequestPlay]
    implicit val requestPingDecoder: Decoder[RequestPing] = deriveDecoder[RequestPing]
    implicit val responsePlayEncoder: Encoder[ResponsePlay] = deriveEncoder[ResponsePlay]
    implicit val responsePongEncoder: Encoder[ResponsePong] = deriveEncoder[ResponsePong]
  }
}
