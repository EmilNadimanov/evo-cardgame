import sbt._

object Dependencies {
  object Versions {
    val catsVersion = "2.7.0"
    val catsEffectVersion = "2.5.1"
    val http4sVersion = "0.21.1"
    val scalatestVersion = "3.2.12"
    val scalacheck = "1.16.0"
    val pureconfigVersion = "0.17.1"
    val scalatagsVersion = "0.11.1"
  }

  import Versions._

  val cats = Seq(
    "org.typelevel" %% "cats-core" % catsVersion,
    "org.typelevel" %% "cats-effect" %  catsEffectVersion,
  )

  val http4s = Seq(
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    "com.lihaoyi" %% "scalatags" % scalatagsVersion

  )

  val configuration = Seq(
    "com.github.pureconfig" %% "pureconfig" % pureconfigVersion,
  )

  val scalatest = Seq(
    "org.scalatest" %% "scalatest" % scalatestVersion % Test,
    "org.scalacheck" %% "scalacheck" % scalacheck % Test,
  )
}
