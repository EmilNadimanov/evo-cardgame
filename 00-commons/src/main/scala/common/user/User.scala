package evo.cardgame
package common.user

import common.cards.card.Card
import common.cards.hand.Hand

trait User[F[_], CardType <: Card] {
  val name: String
  val hand: Hand[F, CardType]
}