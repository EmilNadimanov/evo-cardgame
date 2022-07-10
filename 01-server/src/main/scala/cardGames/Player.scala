package evo.cardgame
package cardGames

import common.cards.card.Card
import common.cards.hand.{Hand, HandImpl}
import common.user.User

import cats.effect.Sync
import cats.implicits.toFunctorOps
import cats.{Functor, Monad}

import java.util.UUID

class Player[F[+_] : Sync : Monad, CardType <: Card](
    val name: String,
    val score: Int,
    val hand: Hand[F, CardType],
) extends User[F, CardType] {
  def takePoints(count: Int): F[Player[F, CardType]] =
    changeScore(score - count)

  private def changeScore(score: Int): F[Player[F, CardType]] =
    Player(name, score, hand.cards)

  def givePoints(count: Int): F[Player[F, CardType]] =
    changeScore(score + count)

  def changeHand(newHand: Hand[F, CardType]): F[Player[F, CardType]] = Sync[F].delay {
    new Player(name, score, newHand)
  }
}


object Player {
  def apply[F[+_] : Sync : Functor, CardType <: Card](
      name: String,
      score: Int,
      cards: Vector[CardType] = Vector.empty
  ): F[Player[F, CardType]] =
    HandImpl[F, CardType](cards)
      .map { hand =>
        new Player(name, score, hand)
      }
}
