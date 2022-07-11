package evo.cardgame.common.cards
package card

import cats.kernel.Comparison
import evo.cardgame.common.cards.Rank._
import evo.cardgame.common.cards.Suit._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CardRankBasedSpec extends AnyWordSpec with Matchers {

  import CardRankBasedSpec._
  import extension.ComparableCard._

  "Rank-based cards" should {
    "compare to other cards according to their rank if they have the same suit" in {
      jackOfSpades.compareTo(aceOfSpades) == Comparison.LessThan &&
        jackOfSpades.compareTo(tenfSpades) == Comparison.GreaterThan &&
        jackOfSpades.compareTo(jackOfSpadesAgain) == Comparison.EqualTo
    }
    "compare to other cards according to their rank if they have different suit" in {
      jackOfSpades.compareTo(kingOfClubs) == Comparison.LessThan &&
        jackOfSpades.compareTo(twoOfDiamonds) == Comparison.GreaterThan &&
        jackOfSpades.compareTo(jackOfHearts) == Comparison.EqualTo
    }

    "provide a collection of all ranks sorted y their power" in {
      val suit = Clubs
      val cards = allRanksInOrder
        .map(rank => CardRankBased(suit, rank))

      cards.zip(cards.tail)
        .forall { case (a, b) => a.compareTo(b) == Comparison.LessThan }

    }
  }
}

object CardRankBasedSpec {
  val jackOfSpades = CardRankBased(Spades, Jack)
  val jackOfSpadesAgain = CardRankBased(Spades, Jack)
  val aceOfSpades = CardRankBased(Spades, Ace)
  val tenfSpades = CardRankBased(Spades, `10`)

  val jackOfHearts = CardRankBased(Hearts, Jack)
  val twoOfDiamonds = CardRankBased(Diamonds, `2`)
  val kingOfClubs = CardRankBased(Clubs, King)
}