enablePlugins(PlayScala)
dependsOn(sharedJVM)

name := """onlineGo"""
organization := "com.fang"

version := "1.0-SNAPSHOT"

lazy val root = project in file(".")

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(
    libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.4.3"
  ).jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJVM = shared.jvm
lazy val sharedJS = shared.js

lazy val js = project.dependsOn(sharedJS)

scalaJSProjects += js

(pipelineStages in Assets) ++= Seq(scalaJSPipeline)
compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value

scalaVersion in Global := "2.11.11"

libraryDependencies ++= Seq(filters)
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
libraryDependencies += "com.pauldijou" %% "jwt-upickle" % "0.13.0"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.fang.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.fang.binders._"
