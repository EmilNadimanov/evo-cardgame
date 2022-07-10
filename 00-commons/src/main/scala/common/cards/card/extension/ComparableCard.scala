package evo.cardgame
package common.cards.card.extension

import common.cards.card.{Card, CardRankBased}

import cats.kernel.{Comparison, Order}

trait ComparableCard[A <: Card] {
  def compare(one: A)(another: A): Comparison
}

object ComparableCard {
  implicit class ComparableCardSyntax[A <: Card](self: A)(implicit comparableCard: ComparableCard[A]) {
    def compareTo(another: A): Comparison =
      comparableCard.compare(self)(another)
  }

  implicit val rankBased = new ComparableCard[CardRankBased] {
    override def compare(self: CardRankBased)(another: CardRankBased): Comparison =
      Order.comparison(self.rank.power, another.rank.power)
  }
}