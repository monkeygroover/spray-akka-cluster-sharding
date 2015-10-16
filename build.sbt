lazy val commonSettings =  Seq(
  organization := "com.monkeygroover"
  , version := "0.1.0-SNAPSHOT"
  , scalaVersion := "2.11.7"
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
      "io.spray" %%  "spray-json"    % "1.3.2"
    )
  )

lazy val backend = project
  .settings(commonSettings:_*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-cluster" % "2.4.0",
      "com.typesafe.akka" %% "akka-cluster-sharding" % "2.4.0",
      "com.github.scullxbones" %% "akka-persistence-mongo-casbah" % "1.0.6",
      "org.mongodb" %% "casbah" % "2.8.2",
      "com.chuusai" %% "shapeless" % "2.2.5"
    )
  ).dependsOn(commands, persistence)

lazy val rest = project
  .settings(commonSettings:_*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-cluster" % "2.4.0",
      "com.typesafe.akka" %% "akka-cluster-sharding" % "2.4.0",
      "io.spray" %%  "spray-can"     % "1.3.3",
      "io.spray" %%  "spray-routing-shapeless2" % "1.3.3",
      "io.spray" %%  "spray-json"    % "1.3.2"
    )
  ).dependsOn(commands, backend)

lazy val seed = project
  .settings(commonSettings:_*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-cluster" % "2.4.0"
    )
  )

lazy val root =
  project.in( file(".") )
    .aggregate(persistence, commands, backend, rest, seed)

