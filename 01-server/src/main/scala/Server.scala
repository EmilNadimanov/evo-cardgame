package evo.cardgame

import cardGames.simpleCardGame.routes.Routes
import cardGames.simpleCardGame.session.Session
import cardGames.utils.CommunicationADTs.Game
import cardGames.utils.Configuration.{Configuration, Rules}
import common.cards.card.CardRankBased
import common.cards.deck.DeckClassic52

import cats.effect._
import cats.effect.concurrent.Ref
import fs2.Stream
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import pureconfig.ConfigSource.default.loadOrThrow
import pureconfig.generic.auto._

import java.util.UUID
import scala.concurrent.duration.DurationInt

object Server extends IOApp {
  private type F[+A]    = IO[A]
  private type DeckType = DeckClassic52[F, CardType]
  private type CardType = CardRankBased

  def stream[F[+ _]: ConcurrentEffect: Timer: ContextShift](
    port: Int,
    //  runningRef: Ref[F, Map[UUID, (Fiber[F, Unit], Session[F, CardType])]],
    pendingTablesRef: Ref[F, Map[Game, Session[F, CardType]]],
    gameRules: Rules,
  ): fs2.Stream[F, ExitCode] = {
    // TODO: find a cleaner way to supply decks for games
    val deckBuider = DeckClassic52(
      CardRankBased.apply
    )
    BlazeServerBuilder[F]
      .bindHttp(port, "0.0.0.0")
      .withHttpApp(
        Router(
          "/" -> new Routes[F, CardType](pendingTablesRef, gameRules, deckBuider).routes
        ).orNotFound
      )
      .serve
  }

  override def run(args: List[String]): F[ExitCode] =
    (for {
      config <- d(loadOrThrow[Configuration])
      port   <- d(config.port)
      // TODO: do I even need to store these sessions? I just want them to run
//      runningSessions    <- Ref.of[F, Map[UUID, (Fiber[F, Unit], Session[F, CardType])]](Map.empty)
      pendingSessionsRef <- Ref.of[F, Map[Game, Session[F, CardType]]](Map.empty)
      gameRules          <- d(config.rules)
      _           <- stream[F](port, pendingSessionsRef, gameRules).compile.drain
    } yield ()).as(ExitCode.Success)

  private def d[A](a: => A): F[A] = Sync[F].delay(a)
}
