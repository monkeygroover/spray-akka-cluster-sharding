enablePlugins(JavaAppPackaging)

lazy val commonSettings = Seq(
  organization := "com.monkeygroover",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.11.7"
)

//lazy val dockerSettings = Seq(
//  dockerExposedPorts in Docker := Seq(8000),
//  dockerEntrypoint in Docker := Seq("sh", "-c", "CLUSTER_IP=`/sbin/ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1 }'` bin/clustering $*"),
//  dockerRepository := Some("monkeygroover"),
//  dockerBaseImage := "java"
//)

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


lazy val backend = project
  .settings(commonSettings:_*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-cluster" % "2.4.0",
      "com.typesafe.akka" %% "akka-cluster-sharding" % "2.4.0",
      "com.github.scullxbones" %% "akka-persistence-mongo-casbah" % "1.0.6",
      "org.mongodb" %% "casbah" % "2.8.2"
    )
  )
  .settings(mainClass in assembly := Some("com.monkeygroover.backend.Bootstrap"))
  .settings(
    dockerExposedPorts in Docker := Seq(8000),
    dockerEntrypoint in Docker := Seq("sh", "-c", "CLUSTER_IP=`/sbin/ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1 }'` bin/clustering $*"),
    dockerRepository := Some("monkeygroover"),
    dockerBaseImage := "java"
  )
  .dependsOn(commands, persistence, service)


lazy val rest = project
  .settings(commonSettings:_*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-cluster" % "2.4.0",
      "com.typesafe.akka" %% "akka-cluster-sharding" % "2.4.0",
      "com.typesafe.akka" %% "akka-http-experimental" % "1.0",
      "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "1.0",
      "io.spray" %%  "spray-json" % "1.3.2",
      // for persistence query..
      "com.typesafe.akka" %% "akka-persistence-query-experimental" % "2.4.0",
      "com.github.scullxbones" %% "akka-persistence-mongo-casbah" % "1.0.6",
      "org.mongodb" %% "casbah" % "2.8.2"
    )
  ).dependsOn(commands, service)
  .settings(mainClass in assembly := Some("com.monkeygroover.frontend.Bootstrap"))

lazy val seed = project
  .settings(commonSettings:_*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-cluster" % "2.4.0"
    )
  )
  .settings(mainClass in assembly := Some("com.monkeygroover.seed.Bootstrap"))

lazy val root =
  project.in( file(".") )
    .aggregate(persistence, commands, service, backend, rest, seed)