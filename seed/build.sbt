enablePlugins(DockerPlugin)

organization := "com.monkeygroover"
version := "0.1.0-SNAPSHOT"
scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-cluster" % "2.4.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3"
)


mainClass in assembly := Some("com.monkeygroover.seed.Bootstrap")

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
