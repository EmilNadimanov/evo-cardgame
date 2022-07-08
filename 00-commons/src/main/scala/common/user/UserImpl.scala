package evo.cardgame
package common.user

import cats.effect.Sync

class UserImpl[F[+_] : Sync](val id: Long, val name: String, val points: Int) extends User[F] {
  override def takePoints(count: Int): F[User[F]] =
    changeScore(points - count)

  override def givePoints(count: Int): F[User[F]] =
    changeScore(points + count)

  private def changeScore(score: Int): F[User[F]] =
    UserImpl(id, name, score)
}


object UserImpl {
  def apply[F[+_] : Sync](id: Long, name: String, points: Int): F[User[F]] =
    Sync[F].delay(
      new UserImpl(id, name, points)
    )
}
