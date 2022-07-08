package evo.cardgame
package common.user

import common.utils.SyncIOSuite

import cats.effect.IO
import evo.cardgame.common.user.UserImplSpec.theUser
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class UserImplSpec extends AnyWordSpec with Matchers with SyncIOSuite {
  "changing points" should {
    "work as expected" in ioTest {
      for {
        user99 <- theUser
        user77 <- user99.takePoints(22)
        user100 <- user77.givePoints(23)
      } yield assert{
        user99.points == 99 && user77.points == 77
      }
    }
  }

}

object UserImplSpec {
  val theUser: IO[User[IO]] = UserImpl[IO](42, "Bobby Fischer", 99)
}