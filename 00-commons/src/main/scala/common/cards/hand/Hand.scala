package evo.cardgame.common.cards
package hand

import cats.Monad
import cats.effect.Sync
import cats.implicits._
import evo.cardgame.common.cards.card.Card

trait Hand[F[_], CardType <: Card[CardType]] {
  val cards: Vector[CardType]

  def addCard(card: CardType): F[Hand[F, CardType]]

  def dropCards(): F[Hand[F, CardType]]

  final def addCards(cards: Vector[CardType])(implicit m: Monad[F], s: Sync[F]): F[Hand[F, CardType]] =
    cards match {
      case head +: tail => addCard(head).flatMap {
        _.addCards(tail)
      }
      case Vector() => Sync[F].delay(this)
    }
}