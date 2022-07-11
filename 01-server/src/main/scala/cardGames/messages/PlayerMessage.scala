package evo.cardgame
package cardGames.messages

sealed trait PlayerMessage extends Message

// NOT FINISHED
object PlayerMessage {
  def parse(userName: String, text: String): PlayerMessage = text match {
    case s"/act/$move" => Move.parse(move) match {
      case Some(moveObj) => MakeMove(userName, moveObj)
      case None => BadInput
    }
  }

  final case class MakeMove(userName: String, move: Move) extends PlayerMessage
  final case class Disconnect(userName: String) extends PlayerMessage
  final case class Connected(userName: String) extends PlayerMessage
  final case object BadInput extends PlayerMessage
}
