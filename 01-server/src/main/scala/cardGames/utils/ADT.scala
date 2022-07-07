package evo.cardgame
package cardGames.utils

class ADT {
  object CardGame {
    sealed trait Move

    final case object Play extends Move

    final case object Fold extends Move
  }
}
