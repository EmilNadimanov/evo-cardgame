package evo.cardgame.common.cards
package hand

import evo.cardgame.common.cards.card.Card

trait Hand[F[_], CardType <: Card] {
  val cards: Vector[CardType]

  def dropCards(): F[Hand[F, CardType]]

  def addCards(cards: Vector[CardType]): F[Hand[F, CardType]]
}