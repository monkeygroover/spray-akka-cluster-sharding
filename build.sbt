name := """cluster-sharding"""

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-cluster" % "2.4.0",
  "com.typesafe.akka" %% "akka-cluster-sharding" % "2.4.0",
  "com.github.scullxbones" %% "akka-persistence-mongo-casbah" % "1.0.6",
  "org.mongodb" %% "casbah" % "2.8.0"
)