package evo.cardgame
package common.cards.hand

import common.cards.card.CardRankBased
import common.utils.Generators._
import common.utils.{SeedWrapper, IOSuite}

import cats.effect.IO
import org.scalacheck.rng.Seed
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class HandImplSpec extends AnyWordSpec with Matchers with IOSuite {

  import HandImplSpec._

  "adding cards" should {
    val handF = handRankBasedF[IO](0)
    "actually add cards to hand" in ioTest {
      for {
        hand <- handF
        initiallyEmpty = hand.cards.isEmpty
        _ = println(hand.cards)
        handOneCard <- hand.addCards(Vector(arb[CardRankBased].gen))
        andThenHasACard = handOneCard.cards.size == 1
      } yield assert(initiallyEmpty && andThenHasACard)
    }
  }
  "dropping cards" should {
    "actually remove cards from hand" in ioTest {
      val initialSize = 5
      val handF = handRankBasedF[IO](initialSize)
      for {
        hand <- handF
        initiallyFull = hand.cards.size == initialSize
        handOneCard <- hand.dropCards()
        andThenEmpty = handOneCard.cards.isEmpty
      } yield assert(initiallyFull && andThenEmpty)
    }
  }
}

object HandImplSpec {
  implicit val seed: SeedWrapper = new SeedWrapper(Seed(42))
}
