package evo.cardgame.common.cards
package card

import cats.kernel.Comparison

abstract class Card[T <: Card[T]] {
  val suit: Suit
  val rank: Rank

  def compareTo(another: T): Comparison
}
