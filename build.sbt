import Dependencies._

ThisBuild / scalaVersion := "2.13.6"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "tcpfsm"
ThisBuild / organizationName := "tcpfsm"

val AkkaVersion = "2.6.15"

lazy val root = (project in file(".")).settings(
  name := "TCP FSM",
  libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test,
      scalaTest % Test))
