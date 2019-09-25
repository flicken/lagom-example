organization in ThisBuild := "net.flicken"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

lazy val `swingtime` = (project in file("."))
  .aggregate(`users-api`, `users-impl`, `projects-api`, `projects-impl`)

val pac4jVersion = "3.6.1"
val lagomPac4j = "org.pac4j" %% "lagom-pac4j" % "2.0.0"
val pac4jHttp = "org.pac4j" % "pac4j-http" % pac4jVersion

lazy val `users-api` = (project in file("users-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `users-impl` = (project in file("users-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      pac4jHttp,
      lagomPac4j,
      macwire,
      scalaTest
    )
  )
  .dependsOn(`users-api`)


lazy val `projects-api` = (project in file("projects-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `projects-impl` = (project in file("projects-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      pac4jHttp,
      lagomPac4j,
      macwire,
      scalaTest
    )
  )
  .dependsOn(`projects-api`, `users-api`)
