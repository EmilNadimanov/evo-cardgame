package evo.cardgame.common.cards
package deck

import cats.Monad
import cats.effect.{Ref, Sync}
import evo.cardgame.common.cards.card.Card

class DeckClassic54[F[_] : Sync : Monad, CardType <: Card[CardType]](val cardFactory: (Suit, Rank) => CardType) extends Deck[F, CardType] {
  override val cards: Ref[F, Vector[CardType]] = Ref.unsafe {
    for {
      suit <- Suit.allSuits
      rank <- Rank.allRanks
    } yield cardFactory(suit, rank)
  }

  override def takeOne(): F[Option[CardType]] =
    cards.modify {
      case card +: rest => rest -> Some(card)
      case _ => Vector() -> None
    }

  override def takeN(n: Int): F[Vector[CardType]] =
    cards.modify(
      _.splitAt(n).swap
    )
}

object DeckClassic54 {
  def apply[F[_] : Sync : Monad, CardType <: Card[CardType]](cardFactory: (Suit, Rank) => CardType): F[DeckClassic54[F, CardType]] =
    Sync[F].delay(DeckClassic54(cardFactory))
}