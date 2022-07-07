package evo.cardgame
package common.cards.hand

import common.cards.card.Card

import cats.Monad
import cats.effect.{Ref, Sync}

class HandImpl[F[_] : Sync : Monad, CardType <: Card[CardType]](val maxCards: Int) extends Hand[F, CardType] {
  override val hand: Ref[F, Vector[CardType]] =
    Ref.unsafe[F, Vector[CardType]](Vector.empty)

  override def addCard(card: CardType): F[Vector[CardType]] =
    hand.updateAndGet { hand =>
      card +: hand
    }
}
