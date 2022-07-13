package evo.cardgame
package cardGames.simpleCardGame

import cardGames.utils.CommunicationADTs.Move
import cardGames.utils.CommunicationADTs.Move._
import cardGames.utils.Configuration.SimpleCardGameConf
import cardGames.{CardGame, Player}
import common.cards.card.Card
import common.cards.card.extension.ComparableCard
import common.cards.card.extension.ComparableCard._
import common.cards.deck.Deck
import common.cards.hand.HandImpl

import cats.effect.Sync
import cats.implicits.{catsSyntaxApplicativeId, toFlatMapOps, toFunctorOps}
import cats.syntax.traverse._
import cats.{Applicative, Comparison}

import java.util.UUID

class SimpleCardGame[F[+_] : Sync : Applicative, CardType <: Card : ComparableCard](
    override val uuid: UUID,
    override val players: Map[String, Player[F, CardType]],
    val gameConf: SimpleCardGameConf,
    override val cardDeck: Deck[F, CardType]) extends CardGame[F, CardType] {

  val DRAW = (0, 0)
  val NUMBER_OF_PLAYERS_TO_PLAY = 2
  val SimpleCardGameConf(handSize, points, _) = gameConf

  import points._

  override def deal(): F[SimpleCardGame[F, CardType]] = {
    if (players.size != NUMBER_OF_PLAYERS_TO_PLAY)
      this.pure[F]
    else
      for {
        currentDeckSize <- cardDeck.size
        numberOfPlayers = players.size
        maybeRenewedDeck <-
          if (currentDeckSize < (handSize * numberOfPlayers))
            cardDeck.pure[F]
          else
            cardDeck.refresh()
        deckAndTakenCards <- takeCards(maybeRenewedDeck, numberOfPlayers, handSize)
        newDeck = deckAndTakenCards._1
        handList <- deckAndTakenCards._2
          .traverse(cards => HandImpl(cards))
        playersWithNewCards <- players.toList
          .zip(handList)
          .traverse { case ((uuid, player), newHand) =>
            player
              .changeHand(newHand)
              .map(player => uuid -> player)
          }
          .map(_.toMap)
      } yield new SimpleCardGame[F, CardType](
        uuid = uuid,
        players = playersWithNewCards,
        gameConf = gameConf,
        cardDeck = newDeck
      )
  }

  def makeMoves(
      player1: Player[F, CardType], move1: Move)(
      player2: Player[F, CardType], move2: Move
  ): F[SimpleCardGame[F, CardType]] =
    for {
      score <- calcPoints(player1.hand, move1)(player2.hand, move2)
      (points1, points2) = score
      player1Updated <- player1.givePoints(points1).flatMap(_.dropCards())
      player2Updated <- player2.givePoints(points2).flatMap(_.dropCards())
      newMap = Map(
        player1Updated.name -> player1Updated,
        player2Updated.name -> player2Updated
      )
    } yield new SimpleCardGame[F, CardType](
      uuid = uuid,
      players = newMap,
      gameConf = gameConf,
      cardDeck = cardDeck
    )

  private def calcPoints(hand1: HandType, move1: Move)(hand2: HandType, move2: Move): F[(Int, Int)] =
    Sync[F].defer {
      (move1, move2) match {
        case (Fold, Fold) => (-foldFoldPoints, -foldFoldPoints).pure[F]
        case (Fold, Play) => (-playFoldPoints, playFoldPoints).pure[F]
        case (Play, Fold) => (playFoldPoints, -playFoldPoints).pure[F]
        case (Play, Play) => compareHands(hand1, hand2)
      }
    }

  private def compareHands(hand1: HandType, hand2: HandType): F[(Int, Int)] =
    Sync[F].delay {
      hand1.cards.sortBy(_.rank.power)(Ordering[Int].reverse).lazyZip {
        hand2.cards.sortBy(_.rank.power)(Ordering[Int].reverse)
      }.map { case (c1, c2) => compareCards(c1, c2) }
        .find(_ != DRAW)
        .getOrElse(DRAW)
    }

  private def compareCards(card1: CardType, card2: CardType): (Int, Int) =
    card1.compareTo(card2) match {
      case Comparison.GreaterThan => (+playPlayPoints, -playPlayPoints)
      case Comparison.LessThan => (-playPlayPoints, +playPlayPoints)
      case Comparison.EqualTo => DRAW
    }

  private def takeCards(deck: Deck[F, CardType], setsToTake: Int, handSize: Int) = {
    def takeCardsRec(
        accum: Vector[Vector[CardType]] = Vector.empty,
        deck: Deck[F, CardType] = deck,
        loopsToGo: Int = setsToTake
    ): F[(Deck[F, CardType], Vector[Vector[CardType]])] =
      Sync[F].defer {
        if (loopsToGo <= 0)
          (deck, accum).pure[F]
        else
          deck.takeN(handSize).flatMap { result =>
            val (newDeck, cards) = result
            takeCardsRec(cards +: accum, newDeck, loopsToGo - 1)
          }
      }

    takeCardsRec()
  }
}
