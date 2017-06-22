enablePlugins(ScalaJSPlugin, ScalaJSWeb)

libraryDependencies += "com.thoughtworks.binding" %%% "dom" % "11.0.0-M3"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

scalaJSUseMainModuleInitializer := true