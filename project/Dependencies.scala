import sbt._

object Dependencies {

  val postgresDriver = "org.postgresql" % "postgresql" % "42.2.8"
  val h2Driver = "com.h2database" % "h2" % "1.4.192"
  val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
  val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test
  val akkaDiscoveryKubernetesApi = "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % "1.0.1"
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
  val playJsonDerivedCodecs = "org.julienrf" %% "play-json-derived-codecs" % "7.0.0"

}
