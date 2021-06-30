import sbt._
import sbt.Keys._

val logbackVersion   = "1.2.3"
val scalatestVersion = "3.0.8"

lazy val root =
  Project(id = "root", base = file("."))
    .enablePlugins(ScalafmtPlugin)
    .settings(
      name := "root",
      scalafmtOnCompile := true,
      skip in publish := true
    )
    .withId("root")
    .settings(commonSettings)
    .aggregate(
      analyzerSystemPipeline,
      datamodel,
      dataExtractor,
      dataProcessing,
      animationStatistic,
      anygenreStatistic,
      totalStatistic,
//      stageJournal
    )

lazy val datamodel = (project in file("./datamodel"))
  .enablePlugins(CloudflowLibraryPlugin)

lazy val analyzerSystemPipeline = (project in file("./analyzer-system-pipeline"))
  .enablePlugins(CloudflowApplicationPlugin)
  .settings(
    commonSettings,
    runLocalConfigFile := Some("analyzer-system-pipeline/src/main/resources/local.conf"),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % scalatestVersion % "test"
    )
  )

lazy val dataExtractor = (project in file("./data-extractor"))
  .enablePlugins(CloudflowAkkaPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "org.scalatest"  %% "scalatest"      % scalatestVersion % "test"
    )
  )
  .dependsOn(datamodel)

lazy val dataProcessing = (project in file("./data-processing"))
  .enablePlugins(CloudflowAkkaPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "org.scalatest"  %% "scalatest"      % scalatestVersion % "test"
    )
  )
  .dependsOn(datamodel)

lazy val animationStatistic = (project in file("./animation-statistic"))
  .enablePlugins(CloudflowAkkaPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "org.scalatest"  %% "scalatest"      % scalatestVersion % "test"
    )
  )
  .dependsOn(datamodel)

lazy val anygenreStatistic = (project in file("./anygenre-statistic"))
  .enablePlugins(CloudflowAkkaPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "org.scalatest"  %% "scalatest"      % scalatestVersion % "test"
    )
  )  .dependsOn(datamodel)

lazy val totalStatistic = (project in file("./total-statistic"))
  .enablePlugins(CloudflowFlinkPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "org.scalatest"  %% "scalatest"      % scalatestVersion % "test"
    )
  ).dependsOn(datamodel)

//lazy val stageJournal = (project in file("./stage-journal"))
//  .enablePlugins(CloudflowSparkPlugin)
//  .settings(
//    commonSettings,
//    libraryDependencies ++= Seq(
//      "ch.qos.logback" % "logback-classic" % logbackVersion,
//      "org.scalatest"  %% "scalatest"      % scalatestVersion % "test"
//    )
//  )
//  .dependsOn(datamodel)

lazy val commonSettings = Seq(
  organization := "com.lightbend.cloudflow",
  headerLicense := Some(HeaderLicense.ALv2("(C) 2016-2020", "Lightbend Inc. <https://www.lightbend.com>")),
  scalaVersion := "2.12.11",
  scalacOptions ++= Seq(
    "-encoding",
    "UTF-8",
    "-target:jvm-1.8",
    "-Xlog-reflective-calls",
    "-Xlint",
    "-Ywarn-unused",
    "-Ywarn-unused-import",
    "-deprecation",
    "-feature",
    "-language:_",
    "-unchecked"
  ),
  scalacOptions in (Compile, console) --= Seq("-Ywarn-unused", "-Ywarn-unused-import"),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value
)

dynverSeparator in ThisBuild := "-"
