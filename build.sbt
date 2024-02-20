ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "webtail"
  )

val spring = "org.springframework.boot"
val springVersion = "3.2.2"

val log4j = "org.apache.logging.log4j"
val log4jVersion = "2.22.1"

libraryDependencies ++= List(
//  spring % "spring-boot-starter-websocket" % springVersion,
//  spring % "spring-boot-starter-test" % springVersion,

  "io.netty" % "netty-all" % "4.1.107.Final", // fixed: needed .Final
  log4j % "log4j-core" % log4jVersion,
  log4j % "log4j-api" % log4jVersion,
  log4j %% "log4j-api-scala" % "13.0.0",
  log4j % "log4j-slf4j-impl" % log4jVersion, // log4j2 binding for slf4j 1.X
  log4j % "log4j-slf4j2-impl" % log4jVersion, // log4j2 binding for slf4j 2.X
)