import Dependencies._

val circeVersion = "0.8.0"

lazy val root = (project in file(".")).
  settings(
    name := "rps",
    scalaVersion := "2.12.2",

    libraryDependencies += "io.buildo" %% "enumero" % "1.2.0",
    libraryDependencies += "io.buildo" %% "enumero-circe-support" % "1.2.0",
    libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.7",
    libraryDependencies += "io.buildo" %% "wiro-http-server" % "0.3.0",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion),
    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % "1.13.5",
      "org.scalatest" %% "scalatest" % "3.0.1",
      "com.typesafe.akka" %% "akka-http-testkit" % "10.0.2"
    ).map(_ % Test),
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % "3.2.0",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.2.0",
      "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"
    ),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    resolvers += Resolver.bintrayRepo("buildo", "maven")
  )
