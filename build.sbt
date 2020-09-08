import Dependencies._

resolvers in ThisBuild += Resolver.jcenterRepo

organization in ThisBuild := "be.reaktika"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.13.1"

// enable HTTPS for dev mode
lagomServiceEnableSsl in ThisBuild := true
lagomServiceGatewayAddress in ThisBuild := "0.0.0.0"
lagomServiceLocatorEnabled in ThisBuild := true

// Disable Cassandra
lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false

lazy val `cookie-backend` =
  (project in file(".")).aggregate(
    `cookie-api`,
    `cookie-impl`,
    `monster-api`,
    `monster-impl`,
    `cookie-analytics-api`,
    `cookie-analytics-impl`)

lazy val `cookie-api` = (project in file("cookie-api")).settings(
  addCompilerPlugin(scalafixSemanticdb),
  libraryDependencies ++=
    Seq(lagomScaladslApi, playJsonDerivedCodecs))

lazy val `cookie-impl` = (project in file("cookie-impl"))
  .enablePlugins(LagomScala)
  .settings(
    addCompilerPlugin(scalafixSemanticdb),
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceJdbc,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest,
      postgresDriver,
      h2Driver,
      lagomScaladslAkkaDiscovery,
      akkaDiscoveryKubernetesApi))
  .settings(lagomForkedTestSettings)
  .settings(
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in packageDoc := false,
    sources in (Compile,doc) := Seq.empty
  )
  .dependsOn(`cookie-api`)

lazy val `cookie-analytics-api` =
  (project in file("cookie-analytics-api")).settings(libraryDependencies ++= Seq(lagomScaladslApi))

lazy val `cookie-analytics-impl` = (project in file("cookie-analytics-impl"))
  .enablePlugins(LagomScala)
  .settings(libraryDependencies ++= Seq(
    lagomScaladslKafkaBroker,
    lagomScaladslTestKit,
    lagomScaladslPersistenceJdbc,
    macwire,
    scalaLogging,
    postgresDriver,
    scalaTest,
    h2Driver,
    lagomScaladslAkkaDiscovery,
    akkaDiscoveryKubernetesApi))
  .settings(lagomForkedTestSettings)
  .dependsOn(`cookie-analytics-api`, `cookie-api`)

lazy val `monster-api` = (project in file("monster-api")).settings(
  addCompilerPlugin(scalafixSemanticdb),
  libraryDependencies ++=
    Seq(lagomScaladslApi, playJsonDerivedCodecs))

lazy val `monster-impl` = (project in file("monster-impl"))
  .enablePlugins(LagomScala)
  .settings(libraryDependencies ++= Seq(
    lagomScaladslPersistenceJdbc,
    lagomScaladslKafkaBroker,
    lagomScaladslTestKit,
    scalaLogging,
    macwire,
    scalaTest,
    h2Driver,
    filters,
    lagomScaladslAkkaDiscovery,
    akkaDiscoveryKubernetesApi))
  .settings(lagomForkedTestSettings)
  .dependsOn(`monster-api`, `cookie-api`)
