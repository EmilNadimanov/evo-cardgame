package evo.cardgame
package cardGames

import cardGames.messages.PlayerMessage
import common.cards.card.CardRankBased
import common.utils.IOSuite

import cats.effect.{IO, _}
import fs2.concurrent.Queue
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PlayerSpec extends AnyWordSpec with Matchers with IOSuite {

  import PlayerSpec._

  "changing points" should {
    "work as expected" in ioTest {
      for {
        player99  <- thePlayer
        player77  <- player99.takePoints(22)
        player100 <- player77.givePoints(23)
      } yield
        assert {
          player99.score == 99 && player77.score == 77 && player100.score == 100
        }
    }
  }

}

object PlayerSpec {
  def thePlayer(implicit c: Concurrent[IO]): IO[Player[IO, CardRankBased]] =
    for {
      queue  <- Queue.circularBuffer[IO, PlayerMessage](1)
      player <- Player[IO, CardRankBased]("Bobby Fischer", 99, queue)
    } yield player

}
