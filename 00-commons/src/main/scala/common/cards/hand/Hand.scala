package evo.cardgame.common.cards
package hand

import cats.effect.Ref
import evo.cardgame.common.cards.card.Card

abstract class Hand[F[_], CardType <: Card[CardType]] {
  val maxCards: Int
  val hand: Ref[F, Vector[CardType]]

  def addCard(card: CardType): F[Vector[CardType]]
}