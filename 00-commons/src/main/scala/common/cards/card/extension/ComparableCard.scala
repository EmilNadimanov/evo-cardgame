package evo.cardgame
package common.cards.card.extension

import common.cards.card.{Card, Card}

import cats.kernel.{Comparison, Order}

trait ComparableCard[A <: Card] {
  def compare(one: A)(another: A): Comparison
}

object ComparableCard {
  implicit class ComparableCardSyntax[A <: Card](self: A)(implicit comparableCard: ComparableCard[A]) {
    def compareTo(another: A): Comparison =
      comparableCard.compare(self)(another)
  }

  implicit val rankBased = new ComparableCard[Card] {
    override def compare(self: Card)(another: Card): Comparison =
      Order.comparison(self.rank.power, another.rank.power)
  }
}