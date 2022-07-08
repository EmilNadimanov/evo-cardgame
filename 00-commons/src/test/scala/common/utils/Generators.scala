package evo.cardgame
package common.utils

import common.cards.card.CardRankBased
import common.cards.hand.HandImpl
import common.cards.{Rank, Suit}

import cats.Monad
import cats.effect.Sync
import org.scalacheck.{Arbitrary, Gen}

object Generators {
  implicit val genInstances: Monad[Gen] = new Monad[Gen] {
    override def map[A, B](fa: Gen[A])(f: A => B): Gen[B] = fa.map(f)

    override def pure[A](x: A): Gen[A] = Gen.const(x)

    override def flatMap[A, B](fa: Gen[A])(f: A => Gen[B]): Gen[B] = fa.flatMap(f)

    override def tailRecM[A, B](a: A)(f: A => Gen[Either[A, B]]): Gen[B] = Gen.tailRecM(a)(f)
  }

  implicit class GenOps[A](val self: Gen[A]) extends AnyVal {
    def gen(implicit seed: SeedWrapper): A = {
      self.apply(Gen.Parameters.default, seed.next).get
    }
  }

  implicit val arbSuit: Arbitrary[Suit] = Arbitrary(Gen.oneOf(Suit.allSuits))

  implicit val arbRank: Arbitrary[Rank] = Arbitrary(Gen.oneOf(Rank.allRanksInOrder))

  implicit val arbRankBasedCard: Arbitrary[CardRankBased] = Arbitrary(for {
    suit <- arb[Suit]
    rank <- arb[Rank]
  } yield new CardRankBased(suit, rank))

  def handRankBasedF[F[+_] : Sync : Monad](size: Int)(implicit seed: SeedWrapper): F[HandImpl[F, CardRankBased]] = {
    val suits = (0 to size).map(_ => arb[Suit].gen)
    val ranks = (0 to size).map(_ => arb[Rank].gen)
    val cards = suits
      .zip(ranks)
      .foldLeft(Vector[CardRankBased]()) {
        case (accum, (suit, rank)) => CardRankBased(suit, rank) +: accum
      }
    HandImpl[F, CardRankBased](cards)
  }

  def arb[A](implicit arbitrary: Arbitrary[A]): Gen[A] = arbitrary.arbitrary
}
