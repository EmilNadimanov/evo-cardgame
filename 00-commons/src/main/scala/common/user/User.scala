package evo.cardgame
package common.user

abstract class User[F[_]] {
  val id: Long
  val name: String
  val points: Int

  def takePoints(count: Int): F[User[F]]

  def givePoints(count: Int): F[User[F]]
}