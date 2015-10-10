name := """cluster-sharding"""

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-cluster" % "2.4.0",
  "com.typesafe.akka" %% "akka-cluster-sharding" % "2.4.0",
  "com.github.scullxbones" %% "akka-persistence-mongo-casbah" % "1.0.6",
  "org.mongodb" %% "casbah" % "2.8.2",
  "io.spray" %%  "spray-can"     % "1.3.3",
  "io.spray" %%  "spray-routing-shapeless2" % "1.3.3",
  "io.spray" %%  "spray-json"    % "1.3.2",
  "com.chuusai" %% "shapeless" % "2.2.5"
)