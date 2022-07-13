package evo.cardgame.common.cards
package card

final class CardRankBased(val suit: Suit,
                          val rank: Rank) extends Card {
  override def toString: String = s"$rank of $suit"
}

object CardRankBased {
  def apply(suit: Suit, rank: Rank): CardRankBased =
    new CardRankBased(suit, rank)
}