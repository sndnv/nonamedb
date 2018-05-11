import sbt.Keys._

name in ThisBuild := "nonamedb"
licenses in ThisBuild := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
homepage in ThisBuild := Some(url("https://github.com/sndnv/nonamedb"))

scalaVersion in ThisBuild := "2.12.6"

lazy val akkaVersion = "2.5.12"
lazy val akkaHttpVersion = "10.1.1"

lazy val nonamedb = (project in file("."))
  .settings(
    crossScalaVersions := Seq("2.12.6"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor"         % akkaVersion,
      "com.typesafe.akka" %% "akka-http"          % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"        % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit"       % akkaVersion       % Test,
      "com.typesafe.akka" %% "akka-http-testkit"  % akkaHttpVersion   % Test,
      "org.scalacheck"    %% "scalacheck"         % "1.14.0"          % Test,
      "org.scalatest"     %% "scalatest"          % "3.0.5"           % Test
    ),
    logBuffered in Test := false,
    parallelExecution in Test := false,
    scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")
  )

addCommandAlias("qa", "; coverage; test; coverageReport")
