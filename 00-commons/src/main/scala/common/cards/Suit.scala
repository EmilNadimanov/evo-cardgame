package evo.cardgame
package common.cards

sealed trait Suit

object Suit {
  val allSuits = Vector(Hearts, Diamonds, Clubs, Spades)

  final case object Hearts extends Suit

  final case object Diamonds extends Suit

  final case object Clubs extends Suit

  final case object Spades extends Suit
}
