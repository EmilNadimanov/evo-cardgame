val CcTt = "compile->compile;test->test"

lazy val commons = project.in(file("00-commons"))
  .settings(
    commonSettings,
    name := "commons",
    libraryDependencies ++= (Dependencies.cats ++
      Dependencies.scalatest),
  )

lazy val server = (project in file("01-server"))
  .dependsOn(commons % CcTt)
  .settings(commonSettings: _*)
  .settings(
    name := "server",
    libraryDependencies ++= (Dependencies.cats ++
      Dependencies.http4s ++
      Dependencies.configuration
      )
  )

lazy val commonSettings = Seq(
  addCompilerPlugin(
    "org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full
  ),
  scalaVersion := "2.13.8",
  idePackagePrefix := Some("evo.cardgame"),
  Compile / console / scalacOptions --= Seq(
    "-deprecation",
    "-feature",
    "-language:_",
    "-unchecked",
    "-Wunused:_",
    "-Xfatal-warnings",
    "-Ymacro-annotations"
  ),
  Test / console / scalacOptions :=
    (Compile / console / scalacOptions).value
)