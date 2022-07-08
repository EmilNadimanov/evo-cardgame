package evo.cardgame
package common.cards

sealed trait Rank {
  def power: Int
}

object Rank {
  val allRanksInOrder: Vector[Rank] = Vector(`2`, `3`, `4`, `5`, `6`, `7`, `8`, `9`, `10`, Jack, Queen, King, Ace)

  final case object `2` extends Rank   {val power = 1 }
  final case object `3` extends Rank   {val power = 2 }
  final case object `4` extends Rank   {val power = 3 }
  final case object `5` extends Rank   {val power = 4 }
  final case object `6` extends Rank   {val power = 5 }
  final case object `7` extends Rank   {val power = 6 }
  final case object `8` extends Rank   {val power = 7 }
  final case object `9` extends Rank   {val power = 8 }
  final case object `10` extends Rank  {val power = 9 }
  final case object Jack extends Rank  {val power = 10 }
  final case object Queen extends Rank {val power = 11 }
  final case object King extends Rank  {val power = 12 }
  final case object Ace extends Rank   {val power = 13 }
}
