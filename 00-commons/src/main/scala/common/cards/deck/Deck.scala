package evo.cardgame.common.cards
package deck

import cats.effect.concurrent.Ref
import evo.cardgame.common.cards.card.Card

abstract class Deck[F[_], CardType <: Card[CardType]] {
  val cards: Ref[F, Vector[CardType]]
  val cardFactory: (Suit, Rank) => CardType

  def takeOne(): F[Option[CardType]]

  def takeN(n: Int): F[Vector[CardType]]
}
