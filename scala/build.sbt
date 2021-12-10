import sbt.Keys._

name := "nonamedb"
licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
homepage := Some(url("https://github.com/sndnv/nonamedb"))

scalaVersion := "2.13.7"

lazy val akkaVersion = "2.6.17"
lazy val akkaHttpVersion = "10.2.7"

lazy val nonamedb = (project in file("."))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "org.scalacheck" %% "scalacheck" % "1.15.4" % Test,
      "org.scalatest" %% "scalatest" % "3.2.10" % Test
    ),
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    Test / logBuffered := false,
  )

addCommandAlias("qa", "; coverage; test; coverageReport")
