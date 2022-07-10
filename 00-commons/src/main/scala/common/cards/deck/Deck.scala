package evo.cardgame.common.cards
package deck

import evo.cardgame.common.cards.card.Card

abstract class Deck[F[+_], CardType <: Card] {
  val cards: F[Vector[CardType]]

  def takeN(n: Int): F[(Deck[F, CardType], Vector[CardType])]

  def refresh(): F[Deck[F, CardType]]

  def size: F[Int]
}
