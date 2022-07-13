package evo.cardgame
package common.utils

import cats.effect.{ContextShift, IO, SyncIO, Timer}
import org.scalatest.{Assertion, Suite}

import scala.concurrent.ExecutionContext

trait IOSuite { this: Suite =>

  implicit lazy val ioContextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit lazy val ioTimer: Timer[IO] = IO.timer(ExecutionContext.global)

  def ioTest(io: IO[Assertion]): Assertion = {
    io.unsafeRunSync()
  }

  def syncIoTest(io: SyncIO[Assertion]): Assertion = {
    io.unsafeRunSync()
  }

}
