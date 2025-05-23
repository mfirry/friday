name := "friday"

val http4sVersion = "1.0.0-M44"
val sttpVersion = "4.0.7"
val circeVersion = "0.14.7"

ThisBuild / scalaVersion := "3.7.0"

lazy val root = (project in file("."))
  .aggregate(common, hardy, laurel)
  .dependsOn(common, hardy, laurel)
  .settings(
    scalafmtOnCompile := true,
    mainClass := Some("friday.Main")
  )

lazy val common = (project in file("common")) // common code
  .settings(
    scalafmtOnCompile := true,
    libraryDependencies ++= Seq(
      "org.jsoup" % "jsoup" % "1.20.1",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion      
    )
  )

lazy val hardy = (project in file("hardy")) // http4
  .settings(
    scalafmtOnCompile := true,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl"          % http4sVersion,      
      "org.http4s" %% "http4s-circe"        % http4sVersion,
      "org.typelevel" % "log4cats-slf4j_3" % "2.7.0"
    )
  ).dependsOn(common)

lazy val laurel = (project in file("laurel")) // sttp
  .settings(
    scalafmtOnCompile := true,
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client4" %% "core" % sttpVersion,
      "com.softwaremill.sttp.client4" %% "cats" % sttpVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "org.typelevel" %% "cats-effect" % "3.5.4", // or latest 3.x

    )
  ).dependsOn(common)
