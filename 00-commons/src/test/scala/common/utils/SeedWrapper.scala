package evo.cardgame
package common.utils

import org.scalacheck.rng.Seed

import java.util.concurrent.atomic.AtomicReference

class SeedWrapper(private val initial: Seed = Seed(0)) {
  private val current = new AtomicReference[Seed](initial)

  def next: Seed = {
    current.updateAndGet(_.next)
  }
}