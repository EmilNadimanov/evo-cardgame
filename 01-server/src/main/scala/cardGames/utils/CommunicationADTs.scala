package evo.cardgame
package cardGames.utils

object CommunicationADTs {
  sealed trait Move
  object Move {
    def parse(move: String): Either[String, Move] = move match {
      case "play" | "Play" => Right(Play)
      case "fold" | "Fold" => Right(Fold)
      case somethingElse@_ => Left(somethingElse)
    }
    final case object Play extends Move
    final case object Fold extends Move
  }

  sealed trait Game
  object Game {
    def parse(game: String): Either[String, Game] = game match {
      case SingleCardGame.toString => Right(SingleCardGame)
      case DoubleCardGame.toString => Right(DoubleCardGame)
      case somethingElse@_ => Left(somethingElse)
    }
    val options = List(SingleCardGame, DoubleCardGame)
    final case object SingleCardGame extends Game
    final case object DoubleCardGame extends Game

  }

  sealed trait DomainError
  object DomainError {
    final case class Unrecognized(valueType: String, input: String) {
      override def toString: String = s"Unrecongnized $valueType encountered: $input"
    }
  }
}
