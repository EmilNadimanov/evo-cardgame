package evo.cardgame
package cardGames.simpleCardGame.utils

object Configuration {
  case class Points(foldFoldPoints: Int, playFoldPoints: Int, playPlayPoints: Int)

  case class SimpleCardGameConf(handSize: Int, points: Points, startingScore: Int)

  case class Rules(
    singleCardGame: SimpleCardGameConf,
    doubleCardGame: SimpleCardGameConf,
  )

  case class Configuration(
    port: Int,
    rules: Rules,
  )
}