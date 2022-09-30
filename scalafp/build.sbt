import sbt.Keys._

name := "nonamedb"
licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
homepage := Some(url("https://github.com/sndnv/nonamedb"))

scalaVersion := "2.13.7"

lazy val jdkDockerImage = "openjdk:11"

lazy val http4sVersion = "1.0.0-M36"

lazy val nonamedb = (project in file("."))
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "com.github.pureconfig" %% "pureconfig" % "0.17.1",
      "org.typelevel" %% "log4cats-slf4j" % "2.5.0",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.scalacheck" %% "scalacheck" % "1.15.4" % Test,
      "org.scalatest" %% "scalatest" % "3.2.10" % Test,
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0" % Test
    ),
    dockerBaseImage := jdkDockerImage,
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    Test / logBuffered := false,
  )
  .enablePlugins(JavaAppPackaging)

addCommandAlias("qa", "; coverage; test; coverageReport")
