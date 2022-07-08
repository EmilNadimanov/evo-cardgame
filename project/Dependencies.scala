import sbt._

object Dependencies {
  object Versions {
    val catsVersion = "2.7.0"
    val catsEffectVersion = "2.5.1"
    val http4s = "0.23.12"
    val scalatestVersion = "3.2.12"
  }

  import Versions._

  val cats = Seq(
    "org.typelevel" %% "cats-core" % catsVersion,
    "org.typelevel" %% "cats-effect" %  catsEffectVersion,
  )

  val http4sClient = Seq(
    "org.http4s" %% "http4s-dsl" % http4s,
    "org.http4s" %% "http4s-circe" % http4s,
    "org.http4s" %% "http4s-blaze-client" % http4s,
  )

  val scalatest = Seq(
    "org.scalatest" %% "scalatest" % scalatestVersion
  )
}
