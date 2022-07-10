package evo.cardgame
package cardGames.routes

import cardGames.Player
import cardGames.messages.PlayerMessage._
import cardGames.messages.ServerMessage.Announce
import cardGames.messages.{Message, PlayerMessage}
import cardGames.simpleCardGame.SimpleCardGame
import cardGames.simpleCardGame.tables.Table
import cardGames.simpleCardGame.utils.CommunicationADTs.Game
import cardGames.simpleCardGame.utils.Configuration.Rules
import common.cards.card.Card
import common.cards.card.extension.ComparableCard
import common.cards.deck.Deck

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.{Blocker, Concurrent, ContextShift}
import cats.implicits.{catsSyntaxApplicativeId, toFlatMapOps, toFunctorOps}
import fs2.Stream
import fs2.concurrent.Topic
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}
import org.http4s.{HttpRoutes, Response, StaticFile}

import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

class Routes[F[+_] : Concurrent : ContextShift : Monad, CardType <: Card : ComparableCard](
    tablesRef: Ref[F, Map[Game, Vector[Table[F, CardType]]]],
    gameRules: Rules,
    deckBuilder: F[Deck[F, CardType]]
) extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    // welcoming web-page with options to choose a game and to provide a username
    case request@GET -> Root =>
      StaticFile
        .fromFile(new File("static/index.html"), blocker, Some(request))
        .getOrElseF(NotFound())

    // find a table for a given game
    case request@GET -> Root / "connect" / gameStr / userName =>
      def userMessages(topic: Topic[F, Message])(wsStream: Stream[F, WebSocketFrame]): Stream[F, Unit] = {
        // parsing user input
        val parsedWebSocketInput: Stream[F, PlayerMessage] =
          wsStream
            .collect {
              case Text(text, _) => PlayerMessage.parse(userName, text)
              case Close(_) => Disconnect(userName)
            }

        // all messages from a user will go into the topic
        parsedWebSocketInput.through(topic.publish)
      }

      // validate game selection
      Game.parse(gameStr) match {
        case None => Response.notFound[F].pure[F]
        case Some(gameType) =>
          val gameConf = gameType match {
            case Game.SingleCardGame => gameRules.singleCardGame
            case Game.DoubleCardGame => gameRules.doubleCardGame
          }
          /*
          1. Find a table with a free spot for a player, and add him to that table.
              Otherwise create a new table for him and save it in `tablesRef`
          2.

           */
          for {
            player <- Player[F, CardType](userName, gameConf.startingScore)
            gameSession <- Topic[F, Message](Announce(""))
            deck <- deckBuilder
            aTable <- tablesRef
              .modify { tableMap: Map[Game, Vector[Table[F, CardType]]] =>
                val sameGame = tableMap
                  .getOrElse(gameType, Vector())

                val aTable = sameGame
                  .find(table => table.game.players.size < 2)
                  .getOrElse {
                    new Table[F, CardType](
                      game = new SimpleCardGame[F, CardType](
                        tableId = UUID.randomUUID(),
                        players = Map(userName -> player),
                        gameConf = gameConf,
                        cardDeck = deck
                      ),
                      session = gameSession
                    )
                  }
                (tableMap + (gameType -> (aTable +: sameGame))) -> aTable
              }
            toUser = gameSession
              .subscribe(1000)
              .map(msg => Text(msg.toString))
            result <- WebSocketBuilder[F].build(toUser, userMessages(gameSession))
          } yield result

      }
  }

  private val blocker: Blocker = {
    val THREADS = 1
    Blocker.liftExecutorService {
      Executors.newFixedThreadPool(THREADS)
    }
  }
}