name := "anime-scraping"

version := "1.0"

scalaVersion := "2.11.5"

resolvers += Resolver.sonatypeRepo("public")

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "3.3.0", // Command parser
  "com.github.nscala-time" %% "nscala-time" % "1.8.0", // JodaTime with scala
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2", // HTTP library
  "nu.validator.htmlparser" % "htmlparser" % "1.4", // HTTP parser
  "com.ibm.icu" % "icu4j" % "54.1.1", // ICU. Character converter
  "com.github.tototoshi" %% "scala-csv" % "1.2.0" // write CSV
)
