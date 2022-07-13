package evo.cardgame
package cardGames

import cardGames.messages.PlayerMessage
import common.cards.card.Card
import common.cards.hand.{Hand, HandImpl}
import common.user.User

import cats.effect.Sync
import cats.implicits.toFunctorOps
import cats.{Functor, Monad}
import fs2.concurrent.Queue

class Player[F[+_] : Sync : Monad, CardType <: Card](
    val name: String,
    val score: Int,
    val actions: Queue[F, PlayerMessage],
    val hand: Hand[F, CardType],
) extends User[F, CardType] {
  def takePoints(count: Int): F[Player[F, CardType]] =
    changeScore(score - count)

  def givePoints(count: Int): F[Player[F, CardType]] =
    changeScore(score + count)

  private def changeScore(score: Int): F[Player[F, CardType]] =
    Player(name, score, actions, hand.cards)

  def changeHand(newHand: Hand[F, CardType]): F[Player[F, CardType]] = Sync[F].delay {
    new Player(name, score, actions, newHand)
  }

  def dropCards(): F[Player[F, CardType]] =
    Player(this.name, this.score, this.actions)
}


object Player {
  def apply[F[+_] : Sync : Functor, CardType <: Card](
      name: String,
      score: Int,
      actions: Queue[F, PlayerMessage],
      cards: Vector[CardType] = Vector.empty,
  ): F[Player[F, CardType]] =
    HandImpl[F, CardType](cards)
      .map { hand =>
        new Player(name, score, actions, hand)
      }
}
