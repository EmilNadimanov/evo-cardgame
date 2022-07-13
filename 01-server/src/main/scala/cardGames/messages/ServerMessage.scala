package evo.cardgame
package cardGames.messages

import cardGames.utils.CommunicationADTs.{Game, Move}

sealed trait ServerMessage extends Message with Product with Serializable{
  def sentTo(targetUser: String): Boolean
}

object ServerMessage {
  sealed trait ForAll extends ServerMessage {
    final override def sentTo(whoever: String): Boolean = true
  }

  final case class SimpleText(username: String, msg: String) extends ServerMessage {
    override def toString: String = s"$msg"
    override def sentTo(targetUser: String): Boolean = (targetUser == username)
  }

  final case class Announce(msg: String) extends ForAll {
    override def toString: String = s"$msg"
  }

  final case object Death extends ForAll {
    override def toString: String = s"Fun is over, session died"
  }

  final case class ReportAction(addressee: String, move: Move) extends ForAll {
    override def toString: String = s"$addressee decided to $move"
  }

  final case object KeepAlive extends ForAll {
    override def toString: String = ""
  }
}
