package evo.cardgame
package utils

import cats.effect.{IO, SyncIO}
import org.scalatest.{Assertion, Suite}

trait SyncIOSuite { this: Suite =>

  def ioTest(io: IO[Assertion]): Assertion = {
    io.unsafeRunSync()
  }

  def syncIoTest(io: SyncIO[Assertion]): Assertion = {
    io.unsafeRunSync()
  }

}
