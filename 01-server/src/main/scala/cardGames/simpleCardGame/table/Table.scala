package evo.cardgame
package cardGames.simpleCardGame.table

import cardGames.messages.{Message, PlayerMessage, ServerMessage}
import cardGames.simpleCardGame.SimpleCardGame
import common.cards.card.Card

import cats.effect.{Concurrent, IO, Sync}
import fs2.Stream
import fs2.concurrent.{Queue, SignallingRef, Topic}

class Table[F[+_]: Concurrent, CardType <: Card](
    val game: SimpleCardGame[F, CardType],
    val session: Topic[F, ServerMessage],
    val playersActions: List[Stream[F, PlayerMessage]]
) {
  // TODO: process messages form topic and react accordingly
  def process(msg: Message): (Table[F, CardType], Seq[Message]) = ???

  // TODO: implement a match of a card game
  def run(): F[Unit] = {
    val playerActionsStream = playersActions.reduce(_ merge _)
      .map {
        case PlayerMessage.MakeMove(userName, move) => ???
        case PlayerMessage.Disconnect(userName) => ???
        case PlayerMessage.Connected(userName) => ???
        case PlayerMessage.BadInput => ???
      }


  }
}

object Tables {
  def apply[F[+_] : Sync, CardType <: Card](
      game: SimpleCardGame[F, CardType],
      session: Topic[F, Message],
  ): F[Table[F, CardType]] = Sync[F].delay(new Table(game, session))
}