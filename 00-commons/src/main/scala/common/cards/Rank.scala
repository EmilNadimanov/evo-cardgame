package evo.cardgame
package common.cards

sealed trait Rank {
  def power: Int
}

object Rank {
  val allRanks = Vector(`2`, `3`, `4`, `5`, `6`, `7`, `8`, `9`, `10`, Jack, Queen, King, Ace)

  final object `2` extends Rank {
    val power = 1
  }

  final object `3` extends Rank {
    val power = 2
  }

  final object `4` extends Rank {
    val power = 3
  }

  final object `5` extends Rank {
    val power = 4
  }

  final object `6` extends Rank {
    val power = 5
  }

  final object `7` extends Rank {
    val power = 6
  }

  final object `8` extends Rank {
    val power = 7
  }

  final object `9` extends Rank {
    val power = 8
  }

  final object `10` extends Rank {
    val power = 9
  }

  final object Jack extends Rank {
    val power = 10
  }

  final object Queen extends Rank {
    val power = 11
  }

  final object King extends Rank {
    val power = 12
  }

  final object Ace extends Rank {
    val power = 13
  }
}
