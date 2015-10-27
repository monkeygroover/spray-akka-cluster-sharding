lazy val commonSettings = Seq(
  organization := "com.monkeygroover",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.11.7"
)

lazy val persistence = project
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "io.spray" %% "spray-json" % "1.3.2"
    )
  )

lazy val commands = project
  .settings(commonSettings:_*)
  .settings(
    libraryDependencies ++= Seq(
      "io.spray" %%  "spray-json" % "1.3.2"
    )
  )

lazy val service = project
  .settings(commonSettings:_*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-cluster" % "2.4.0",
      "com.typesafe.akka" %% "akka-cluster-sharding" % "2.4.0",
      "com.chuusai" %% "shapeless" % "2.2.5"
    )
  )
  .dependsOn(commands, persistence)