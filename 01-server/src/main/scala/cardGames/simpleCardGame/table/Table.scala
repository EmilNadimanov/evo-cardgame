package evo.cardgame
package cardGames.simpleCardGame.table

import cardGames.messages.Message
import cardGames.simpleCardGame.SimpleCardGame
import common.cards.card.Card

import cats.effect.Sync
import fs2.concurrent.Topic

class Table[F[+_], CardType <: Card](
    val game: SimpleCardGame[F, CardType],
    val session: Topic[F, Message],
) {
  // TODO: process messages form topic and react accordingly
  def process(msg: Message): (Table[F, CardType], Seq[Message]) = ???

  // TODO: implement a round of a card game
  def runRound(): F[Unit] = ???
}

object Tables {
  def apply[F[+_] : Sync, CardType <: Card](
      game: SimpleCardGame[F, CardType],
      session: Topic[F, Message],
  ): F[Table[F, CardType]] = Sync[F].delay(new Table(game, session))
}