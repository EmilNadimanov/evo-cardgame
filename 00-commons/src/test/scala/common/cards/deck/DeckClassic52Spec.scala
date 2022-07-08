package evo.cardgame.common.cards
package deck

import cats.effect.IO
import evo.cardgame.common.cards.card.CardRankBased
import evo.cardgame.common.cards.deck.DeckClassic52Spec.theDeck
import evo.cardgame.common.utils.SyncIOSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DeckClassic52Spec extends AnyWordSpec with Matchers with SyncIOSuite {

  "classic 52-card deck" should {
    "contain 52 cards" in ioTest {
      for {
        deck <- theDeck
        cards <- deck.cards.get
      } yield assert(cards.size == 52)
    }
    "contain 13 cards for each suite" in ioTest {
      for {
        deck <- theDeck
        cards <- deck.cards.get
      } yield assert {
        cards
          .groupBy(_.suit)
          .values
          .forall(_.size == 13)
      }
    }
  }

  "Methods should work" when {
    "takeN() is used" in ioTest {
      for {
        deck <- theDeck
        takenCards <- deck.takeN(5)
        remainingSize <- deck.size
      } yield assert {
        takenCards.size == 5 &&
          remainingSize == (52 - 5)
      }
    }
    "takeOne() used" in ioTest {
      for {
        deck <- theDeck
        takenCards <- deck.takeN(5)
        remainingSize <- deck.size
      } yield assert {
        takenCards.size == 5 &&
          remainingSize == (52 - 5)
      }
    }
    "when N cards is taken from a deck smaller than N" in ioTest {
      val firstTake = 50
      val secondTake = 4
      val secondTakeActualSize = 52 - firstTake
      for {
        deck <- theDeck
        _ <- deck.takeN(firstTake)
        asManyAsPossible <- deck.takeN(secondTake)
        remainingSize <- deck.size
      } yield assert {
        asManyAsPossible.size == secondTakeActualSize &&
          remainingSize == 0
      }
    }
    "when one card is taken from an empty deck" in ioTest {
      val firstTake = 52
      val secondTake = 4
      for {
        deck <- theDeck
        _ <- deck.takeN(firstTake)
        noCards <- deck.takeN(secondTake)
        remainingSize <- deck.size
      } yield assert {
        noCards.isEmpty &&
          remainingSize == 0
      }
    }
  }
}

object DeckClassic52Spec {
  val theDeck = DeckClassic52[IO, CardRankBased](CardRankBased.apply)
}