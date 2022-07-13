package evo.cardgame
package cardGames.simpleCardGame.routes

import cardGames.Player
import cardGames.messages.PlayerMessage._
import cardGames.messages.ServerMessage.Announce
import cardGames.messages.{PlayerMessage, ServerMessage}
import cardGames.simpleCardGame.SimpleCardGame
import cardGames.simpleCardGame.session.Session
import cardGames.utils.CommunicationADTs.{Game, Move}
import cardGames.utils.Configuration.Rules
import common.cards.card.Card
import common.cards.card.extension.ComparableCard
import common.cards.deck.Deck

import cats.effect.concurrent.{Deferred, Ref}
import cats.effect.{Blocker, Concurrent, ContextShift, Fiber, Sync}
import cats.implicits._
import fs2.Stream
import fs2.concurrent.{Queue, Topic}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}
import org.http4s.{HttpRoutes, Response, StaticFile, Status}

import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

// TODO: Maybe avoid passing cardType? What if different game use different card types?
class Routes[F[+ _]: Concurrent: ContextShift: Sync, CardType <: Card: ComparableCard](
  // TODO: do I even need to store these sessions? I just want them to run
  runningRef: Ref[F, Map[UUID, (Fiber[F, Unit], Session[F, CardType])]],
  pendingTablesRef: Ref[F, Map[Game, Session[F, CardType]]],
  gameRules: Rules,
  // TODO: find a cleaner way to create decks for games
  deckBuilder: F[Deck[F, CardType]]
) extends Http4sDsl[F] {
  val QUEUE_MAX_SIZE = 10

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    // welcoming web-page with options to choose a game and to provide a username
    case request @ GET -> Root / "simpleCardGame" =>
      StaticFile
        .fromFile(new File("static/index.html"), blocker, Some(request))
        .getOrElseF(NotFound())

    case request @ GET -> Root / "simpleCardGame" / "connect" / gameStr / userName =>
      def fromUserToQueue(queue: Queue[F, PlayerMessage])(wsStream: Stream[F, WebSocketFrame]): Stream[F, Unit] =
        (Stream.emit(Connected(userName)) ++
        wsStream.collect {
          case Text(text, _) => PlayerMessage.parse(userName, text)
          case Close(_)      => Disconnect(userName)
        }).through(queue.enqueue)

      def toUserFromTopic(topic: Topic[F, ServerMessage]): Stream[F, WebSocketFrame] =
        topic
          .subscribe(100)
          .filter(_.sentTo(userName))
          .map(msg => Text(msg.toString))

      Game.parse(gameStr) match {
        case Left(value) =>
          val status = Status(
            code = BadRequest.code,
            reason = s"No such game: $value. Possible options: ${Game.options.mkString(", ")}")
          BadRequest().map(_.withStatus(status))
        case Right(gameType) =>
          val rules = gameType match {
            case Game.SingleCardGame => gameRules.doubleCardGame
            case Game.DoubleCardGame => gameRules.singleCardGame
          }
          for {
            topicDef      <- Deferred[F, Topic[F, ServerMessage]]
            actions       <- Queue.circularBuffer[F, PlayerMessage](QUEUE_MAX_SIZE)
            player        <- Player[F, CardType](userName, rules.startingScore, actions)
            deck          <- deckBuilder
            pendingTables <- pendingTablesRef.get
            maybeNewPendingSession <- pendingTables.get(gameType) match {
              // Add player to an existing pending session, making it ready to run.
              // Run this session and remove from pending
              case Some(pendingSession) =>
                for {
                  pendingSessionGame <- pendingSession.gameStateRef.get
                  updatedGame <- Ref.of {
                    new SimpleCardGame[F, CardType](
                      uuid = pendingSessionGame.uuid,
                      players = pendingSessionGame.players + (userName -> player),
                      gameConf = pendingSessionGame.gameConf,
                      cardDeck = pendingSessionGame.cardDeck,
                    )
                  }
                  readySession = new Session[F, CardType](
                    gameStateRef = updatedGame,
                    serverTopic = pendingSession.serverTopic,
                    movesRef = pendingSession.movesRef
                  )
                  fiber <- Concurrent[F].start(readySession.run())
                  _ <- runningRef.update(_.updated(pendingSessionGame.uuid, (fiber, readySession)))
                  _ <- topicDef.complete(pendingSession.serverTopic)
                } yield None

              // create a new pending session for the player. Pending cuz he will need another guy in it to play
              case None =>
                for {
                  newTopic <- Topic[F, ServerMessage](Announce("Starting session. Waiting for players..."))
                  newGame <- Ref.of(
                    new SimpleCardGame[F, CardType](
                      uuid = UUID.randomUUID(),
                      players = Map(userName -> player),
                      gameConf = rules,
                      cardDeck = deck
                    )
                  )
                  newMovesMap <- Ref.of(Map.empty[String, Move])
                  newTable = new Session[F, CardType](
                    gameStateRef = newGame,
                    serverTopic = newTopic,
                    movesRef = newMovesMap
                  )
                  _ <- topicDef.complete(newTopic)
                } yield Some(newTable)
            }
            _ <- pendingTablesRef.update { existing =>
              if (maybeNewPendingSession.nonEmpty)
                existing + (gameType -> maybeNewPendingSession.get)
              else
                existing - gameType
            }
            topic     <- topicDef.get
            webSocket <- WebSocketBuilder[F].build(toUserFromTopic(topic), fromUserToQueue(actions))
          } yield webSocket

      }

  }

  private val blocker: Blocker = {
    val THREADS = 1
    Blocker.liftExecutorService {
      Executors.newFixedThreadPool(THREADS)
    }
  }
}

object Routes {}
