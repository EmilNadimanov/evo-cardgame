package evo.cardgame

import cardGames.simpleCardGame.utils.Configuration.Configuration
import common.cards.card.CardRankBased
import common.cards.deck.DeckClassic52

import cats.effect._
import pureconfig.ConfigSource.default.loadOrThrow
import pureconfig.generic.auto._

object Server extends IOApp {
  type F[+A] = IO[A]
  type DeckType = DeckClassic52[F, CardType]
  type CardType = CardRankBased

  /*
    def stream[F[_] : ConcurrentEffect : Timer : ContextShift](
        port: Int,
        gameRules: Rules,
        tables: Ref[F, Map[Game, Vector[Table[F, CardType]]]],
    ): fs2.Stream[F, ExitCode] = {
      val deckBuider = DeckClassic52(
        CardRankBased.apply
      )
      BlazeServerBuilder[F]
        .bindHttp(port, "0.0.0.0")
        .withHttpApp(
          Router(
            "/" -> new Routes[F, CardType](tables, gameRules, deckBuider).routes
          ).orNotFound
        )
        .serve
    }
   */

  override def run(args: List[String]): IO[ExitCode] = (for {
    config <- d(loadOrThrow[Configuration])
    port <- d(config.port)
  } yield println(config, port))
    .as(ExitCode.Success)

  private def d[A](a: => A): F[A] = Sync[F].delay(a)
}