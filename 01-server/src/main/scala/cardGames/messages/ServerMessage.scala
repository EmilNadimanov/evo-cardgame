package evo.cardgame
package cardGames.messages

import cardGames.simpleCardGame.utils.CommunicationADTs.Game

sealed trait ServerMessage extends Message

object ServerMessage {
  final case class WelcomeUser(user: String, game: Game) extends ServerMessage {
    override def toString: String = s"Welcome to the $game, dear $user"
  }

  final case class Announce(msg: String) extends ServerMessage {
    override def toString: String = msg
  }

  final case class SendToUsers(users: Set[String], text: String) extends ServerMessage {
    override def toString: String = text
  }

  final case object KeepAlive extends ServerMessage {
    override def toString: String = ""

  }
}
