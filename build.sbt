import scala.language.postfixOps

name := "testEmbedika"

version := "0.1"

scalaVersion := "2.13.8"

scalacOptions ++= Seq("-Ymacro-annotations")

libraryDependencies ++= Seq(
  // doobie
  "org.tpolecat" %% "doobie-core" % "1.0.0-RC1",
  "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC1",
  "org.tpolecat" %% "doobie-postgres-circe" % "1.0.0-RC1",
  "org.tpolecat" %% "doobie-scalatest" % "1.0.0-RC1" % Test,
  // circe
  "io.circe" %% "circe-core" % "0.14.1",
  "io.circe" %% "circe-generic" % "0.14.1",
  "io.circe" %% "circe-parser" % "0.14.1",
  // redis
  "dev.profunktor" %% "redis4cats-effects" % "1.0.0",
  "dev.profunktor" %% "redis4cats-log4cats" % "1.0.0",
  // logger
  "org.typelevel" %% "log4cats-slf4j" % "2.1.1",
  "org.typelevel" %% "log4cats-noop" % "2.1.1",
  "ch.qos.logback" % "logback-classic" % "1.2.6",
  // http4s
  "org.http4s" %% "http4s-dsl" % "0.23.1",
  "org.http4s" %% "http4s-ember-server" % "0.23.1",
  "org.http4s" %% "http4s-ember-client" % "0.23.1",
  "org.http4s" %% "http4s-circe" % "0.23.1",
  // cats/cats-effect
  "org.typelevel" %% "cats-core" % "2.8.0",
  "org.typelevel" %% "cats-effect" % "3.3.14",
  // pureconfig
  "com.github.pureconfig" %% "pureconfig" % "0.17.0",
  // newtype
  "io.estatico" %% "newtype" % "0.4.4",
  // jodatime
  "joda-time" % "joda-time" % "2.10.14",
  // scalatest
  "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0" % Test,
  "org.mockito" %% "mockito-scala-cats" % "1.17.7" % Test,
  "org.scalatest" %% "scalatest" % "3.2.12" % Test,
  // caliban for graphql
  "com.github.ghostdogpr" %% "caliban" % "2.0.1",
  "com.github.ghostdogpr" %% "caliban-http4s" % "2.0.1",
  "com.github.ghostdogpr" %% "caliban-cats" % "2.0.1",
  "dev.zio" %% "zio-interop-cats" % "3.3.0"
)
