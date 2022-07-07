package evo.cardgame
package common.cards

sealed trait Suit

object Suit {
  val allSuits = Vector(Hearts, Diamonds, Clubs, Spades)

  final object Hearts extends Suit

  final object Diamonds extends Suit

  final object Clubs extends Suit

  final object Spades extends Suit
}
