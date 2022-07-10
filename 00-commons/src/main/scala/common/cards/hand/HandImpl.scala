package evo.cardgame
package common.cards.hand

import common.cards.card.Card

import cats.Monad
import cats.effect.Sync

class HandImpl[F[+_] : Sync : Monad, CardType <: Card](
    override val cards: Vector[CardType]
) extends Hand[F, CardType] {

  override def addCard(card: CardType): F[Hand[F, CardType]] =
    HandImpl[F, CardType](card +: cards)

  override def dropCards(): F[Hand[F, CardType]] =
    HandImpl[F, CardType]()
}

object HandImpl {
  def apply[F[+_] : Sync : Monad, CardType <: Card](
      cards: Vector[CardType] = Vector.empty
  ): F[HandImpl[F, CardType]] =
    Sync[F].delay {
      new HandImpl[F, CardType](cards)
    }
}
