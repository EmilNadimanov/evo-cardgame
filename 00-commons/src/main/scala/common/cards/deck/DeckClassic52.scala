package evo.cardgame.common.cards
package deck

import cats.Monad
import cats.effect.Sync
import cats.implicits.{toFlatMapOps, toFunctorOps}
import evo.cardgame.common.cards.card.Card

import scala.util.Random

class DeckClassic52[F[+_] : Sync : Monad, CardType <: Card](
    val cardsOption: Option[Vector[CardType]] = None,
    val cardFactory: (Suit, Rank) => CardType
) extends Deck[F, CardType] {
  override val cards: Vector[CardType] =
      cardsOption.getOrElse {
        Random.shuffle(for {
          suit <- Suit.allSuits
          rank <- Rank.allRanksInOrder
        } yield cardFactory(suit, rank))
      }

  override def takeN(n: Int): F[(DeckClassic52[F, CardType], Vector[CardType])] =
    Sync[F].defer {
      cards.splitAt(n) match {
        case (take, rest) =>
          DeckClassic52(cardFactory, Some(rest))
            .map(_ -> take)
      }
    }

  override def size: F[Int] = Sync[F].delay(cards.size)

  override def refresh(): F[DeckClassic52[F, CardType]] =
    DeckClassic52(cardFactory)
}

object DeckClassic52 {
  def apply[F[+_] : Sync : Monad, CardType <: Card](
      cardFactory: (Suit, Rank) => CardType,
      cardsOption: Option[Vector[CardType]] = None,
  ): F[DeckClassic52[F, CardType]] =
    Sync[F].delay {
      new DeckClassic52[F, CardType](
        cardsOption,
        cardFactory
      )
    }
}