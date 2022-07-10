package evo.cardgame
package cardGames

import common.cards.card.CardRankBased
import common.utils.SyncIOSuite

import cats.effect.IO
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PlayerSpec extends AnyWordSpec with Matchers with SyncIOSuite {

  import PlayerSpec._

  "changing points" should {
    "work as expected" in ioTest {
      for {
        player99 <- thePlayer
        player77 <- player99.takePoints(22)
        player100 <- player77.givePoints(23)
      } yield assert {
        player99.points == 99 && player77.points == 77 && player100.points == 100
      }
    }
  }

}

object PlayerSpec {
  val thePlayer: IO[Player[IO, CardRankBased]] = Player(42, "Bobby Fischer", 99)
}
