name := """cluster-sharding"""

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-contrib" % "2.3.9",
  "com.github.scullxbones" % "akka-persistence-mongo-casbah_2.11" % "1.0.6"
)