package evo.cardgame
package cardGames.simpleCardGame.utils

object CommunicationADTs {
  sealed trait Move
  object Move {
    def parse(move: String): Option[Move] = move match {
      case "play" | "Play" => Some(Play)
      case "fold" | "Fold" => Some(Fold)
      case _ => None
    }
    final case object Play extends Move
    final case object Fold extends Move
  }

  sealed trait Game
  object Game {
    def parse(game: String): Option[Game] = game match {
      case "SingleCardGame" | "SCG" => Some(SingleCardGame)
      case "DoubleCardGame" | "DCG" => Some(DoubleCardGame)
      case _ => None
    }
    final case object SingleCardGame extends Game {
      override def toString: String = "Single card game"
    }
    final case object DoubleCardGame extends Game {
      override def toString: String = "Double card game"
    }
  }
}
