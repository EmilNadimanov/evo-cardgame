package evo.cardgame
package cardGames.messages

import cardGames.utils.CommunicationADTs.Move

sealed trait PlayerMessage extends Message

// NOT FINISHED
object PlayerMessage {
  def parse(userName: String, text: String): PlayerMessage = text match {
    case s"$move" => Move.parse(move) match {
      case Right(moveObj) => MakeMove(userName, moveObj)
      case Left(badInput) => BadInput(userName, badInput)
    }
  }

  final case class MakeMove(userName: String, move: Move) extends PlayerMessage
  final case class Disconnect(userName: String) extends PlayerMessage
  final case class Connected(userName: String) extends PlayerMessage
  final case class BadInput(userName: String, input: String) extends PlayerMessage
}
