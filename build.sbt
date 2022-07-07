lazy val `commons-common` = project.in(file("00-commons"))
  .settings(
    commonSettings,
    name := "commons",
    libraryDependencies ++= Dependencies.cats,
  )

lazy val server = (project in file("01-server"))
  .dependsOn(`commons-common`)
  .settings(commonSettings: _*)
  .settings(
    name := "server",
    libraryDependencies ++= Seq(
      Dependencies.cats
    ).flatten
  )

lazy val client = (project in file("02-client"))
  .dependsOn(`commons-common`)
  .settings(commonSettings: _*)
  .settings(
    name := "client",
    libraryDependencies ++= Seq(
      Dependencies.cats
    ).flatten
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