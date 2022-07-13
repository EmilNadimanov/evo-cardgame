package evo.cardgame
package cardGames

import cardGames.simpleCardGame.SimpleCardGame
import common.cards.card.Card
import common.cards.deck.Deck
import common.cards.hand.Hand

import java.util.UUID

trait CardGame[F[+_], CardType <: Card] {
  type DeckType = Deck[F, CardType]
  type HandType = Hand[F, CardType]

  val uuid: UUID
  val players: Map[String, Player[F, CardType]]
  val cardDeck: Deck[F, CardType]

  def deal(): F[SimpleCardGame[F, CardType]]
}
