package evo.cardgame
package cardGames.simpleCardGame.session

import cardGames.messages.{PlayerMessage, ServerMessage}
import cardGames.messages.ServerMessage.{Announce, SimpleText}
import cardGames.simpleCardGame.SimpleCardGame
import cardGames.utils.CommunicationADTs.Move
import common.cards.card.Card

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Sync}
import cats.implicits._
import fs2.concurrent.Topic
import fs2.Stream

class Session[F[+_] : Concurrent, CardType <: Card](
    val gameStateRef: Ref[F, SimpleCardGame[F, CardType]],
    val serverTopic: Topic[F, ServerMessage],
    val movesRef: Ref[F, Map[String, Move]]
) {
  // TODO: implement a session of a card game
  def run() = {
    val parsingPlayerActions = Stream.eval{
      for {
        currentGameState <- gameStateRef.get
        _ <- parsePlayerActions(currentGameState)
      } yield ()
    }.repeat
    val dealAndMakeMoves = Stream.eval {
      for {
        currentGameState <- gameStateRef.get
        gameState <- dealIfNeeded(currentGameState)
        gameState <- makeMoves(gameState)
        _ <- gameStateRef.set(gameState)
      } yield ()
    }.repeat
    parsingPlayerActions.concurrently(dealAndMakeMoves).compile.drain
  }

  private def parsePlayerActions(currentGameState: SimpleCardGame[F, CardType]): F[Unit] = {
    def reactToMovesPipe(playerActions: Stream[F, PlayerMessage]): Stream[F, Unit] = {
      playerActions.evalMap {
        case PlayerMessage.Disconnect(userName) => Announce(s"$userName left the game. The game will end soon").pure[F]
        case PlayerMessage.Connected(userName) => Announce(s"$userName joined the game").pure[F]
        case PlayerMessage.BadInput(userName, badInput) => SimpleText(userName, s"Invalid input: $badInput").pure[F]
        case PlayerMessage.MakeMove(userName, move) =>
          movesRef.modify { map =>
            map.get(userName) match {
              case Some(previousMove) =>
                (map, SimpleText(userName, s"You already decided to $previousMove"))
              case None =>
                (map.updated(userName, move), Announce(s"$userName decided fto $move"))
            }
          }
      }.through(serverTopic.publish)
    }
    currentGameState.players.values
      .map(_.actions.dequeue)
      .reduce(_ concurrently _)
      .through(reactToMovesPipe)
      .compile.drain
  }

  private def dealIfNeeded(currentGameState: SimpleCardGame[F, CardType]): F[SimpleCardGame[F, CardType]] = for {
    needToDeal <- currentGameState.players.values
      .map(_.hand.cards)
      .exists(_.isEmpty)
      .pure[F]
    maybeUpdatedGameState <- if (needToDeal) currentGameState.deal() else currentGameState.pure[F]
    _ <- if (needToDeal) {
      serverTopic.publish {
        Stream.emits(maybeUpdatedGameState.players.values.toList
          .map { p => SimpleText(p.name, s"Dear ${p.name}, your cards are: ${p.hand.cards.mkString(", ")}") }
        )
      }.compile.drain
    }
    else ().pure[F]
  } yield maybeUpdatedGameState

  private def makeMoves(gameState: SimpleCardGame[F, CardType]): F[SimpleCardGame[F, CardType]] =
    for {
      movesToMake <- movesRef.modify {
        existingMoves =>
          val usernames = gameState.players.values.map(_.name).toSet
          val allPlayersMadeMove = existingMoves.keys.forall(usernames.contains)
          if (allPlayersMadeMove)
            (Map.empty, existingMoves)
          else
            (existingMoves, Map.empty[String, Move])
      }
      effectOptional = for {
        player1AndMove <- movesToMake.headOption
        player1 <- gameState.players.get(player1AndMove._1)
        player2AndMove <- movesToMake.tail.headOption
        player2 <- gameState.players.get(player2AndMove._1)
      } yield gameState.makeMoves(player1, player1AndMove._2)(player2, player2AndMove._2)
      possiblyNewState <- effectOptional match {
        case Some(newState) => newState.flatMap { state =>
            serverTopic.publish {
              Stream.emits(state.players.values.toList
                .map { p => Announce(s"${p.name} has ${p.score} poins") }
                .prepended(Announce(s"SCORE:"))
              )
            }.compile.drain
        } *> newState
        case None => gameState.pure[F]
      }
    } yield possiblyNewState
}

object Session {
  def apply[F[+_] : Sync : Concurrent, CardType <: Card](
      game: SimpleCardGame[F, CardType],
      session: Topic[F, ServerMessage],
  ): F[Session[F, CardType]] = for {
    game <- Ref.of(game)
    moves <- Ref.of(Map.empty[String, Move])
  } yield new Session(game, session, moves)
}