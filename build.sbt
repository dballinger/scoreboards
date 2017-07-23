name := "scoreboards"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.seleniumhq.selenium" % "selenium-server" % "2.47.1",
  "com.github.tototoshi" %% "scala-csv" % "1.3.4",
  "org.scalatest" % "scalatest_2.11" % "2.2.5" % "test"
)