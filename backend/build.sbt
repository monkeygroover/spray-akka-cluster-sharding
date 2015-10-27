enablePlugins(DockerPlugin)

organization := "com.monkeygroover"
version := "0.1.0-SNAPSHOT"
scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-cluster" % "2.4.0",
  "com.typesafe.akka" %% "akka-cluster-sharding" % "2.4.0",
  "com.github.scullxbones" %% "akka-persistence-mongo-casbah" % "1.0.6",
  "org.mongodb" %% "casbah" % "2.8.2",
  "com.monkeygroover" %% "commands" % "0.1.0-SNAPSHOT",
  "com.monkeygroover" %% "persistence" % "0.1.0-SNAPSHOT",
  "com.monkeygroover" %% "service" % "0.1.0-SNAPSHOT"
)

mainClass in assembly := Some("com.monkeygroover.backend.Bootstrap")


docker <<= (docker dependsOn assembly)

buildOptions in docker := BuildOptions(cache = false)

dockerfile in docker := {
  val jarFile = (assemblyOutputPath in assembly).value
  val appDirPath = "/app"
  val jarTargetPath = s"$appDirPath/${jarFile.name}"

  new Dockerfile {
    from("java")
    add(jarFile, jarTargetPath)
    workDir(appDirPath)
    entryPoint("java", "-jar", jarTargetPath)
  }
}