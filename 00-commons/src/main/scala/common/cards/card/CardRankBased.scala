package evo.cardgame.common.cards
package card

import cats.Comparison
import cats.kernel.Order

final class CardRankBased(val suit: Suit,
                          val rank: Rank) extends Card[CardRankBased] {
  override def compare(another: CardRankBased): Comparison =
    Order.comparison(this.rank.power, another.rank.power)

  // TODO: pretty toString
  override def toString: String = ???
}

object CardRankBased {
  def apply(suit: Suit, rank: Rank): CardRankBased =
    new CardRankBased(suit, rank)
}