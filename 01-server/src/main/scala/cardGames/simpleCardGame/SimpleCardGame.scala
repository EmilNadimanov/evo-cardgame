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
    override val tableId: UUID,
    override val players: Map[String, Player[F, CardType]],
    val gameConf: SimpleCardGameConf,
    override val cardDeck: Deck[F, CardType]) extends CardGame[F, CardType] {

  val DRAW = (0, 0)
  val SimpleCardGameConf(handSize, points, _) = gameConf

  import points._

  override def deal(): F[SimpleCardGame[F, CardType]] =
    for {
      currentDeck <- Sync[F].delay(cardDeck)
      playersMap <- Sync[F].delay(players)
      currentDeckSize <- currentDeck.size
      deck <-
        if (currentDeckSize < (handSize * playersMap.size))
          currentDeck.pure[F]
        else
          currentDeck.refresh()
      takeCards <- (1 to playersMap.size).toList
        .traverse(_ => deck.takeN(handSize))
      newDeck = takeCards.last._1
      handList <- takeCards
        .map(_._2)
        .traverse(cards => HandImpl(cards))
      playersWithNewCards <- playersMap.toList
        .zip(handList)
        .traverse { case ((uuid, player), newHand) =>
          player
            .changeHand(newHand)
            .map(player => uuid -> player)
        }
        .map(_.toMap)
    } yield new SimpleCardGame[F, CardType](
      tableId = tableId,
      players = playersWithNewCards,
      gameConf = gameConf,
      cardDeck = newDeck
    )

  def parseMove(
      player1: Player[F, CardType], move1: Move)(
      player2: Player[F, CardType], move2: Move
  ): F[SimpleCardGame[F, CardType]] =
    for {
      score <- calcPoints(player1.hand, move1)(player1.hand, move2)
      (points1, points2) = score
      player1Updated <- player1.givePoints(points1)
      player2Updated <- player2.givePoints(points2)
      newMap = Map(
        player1Updated.name -> player1Updated,
        player2Updated.name -> player2Updated
      )
    } yield new SimpleCardGame[F, CardType](
      tableId = tableId,
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
      case Comparison.GreaterThan => (playPlayPoints, playPlayPoints)
      case Comparison.LessThan => (playPlayPoints, playPlayPoints)
      case Comparison.EqualTo => DRAW
    }
}