package evo.cardgame.common.cards
package card

final class CardRankBased(val suit: Suit,
                          val rank: Rank) extends Card {
  // TODO: pretty toString
  override def toString: String = s"Card($suit, $rank)"
}

object CardRankBased {
  def apply(suit: Suit, rank: Rank): CardRankBased =
    new CardRankBased(suit, rank)
}