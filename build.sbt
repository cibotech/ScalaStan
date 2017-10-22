name := "ScalaStan"

version := "0.1"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6",
  "org.scala-lang"         % "scala-reflect"             % "2.11.8",
  "org.scalatest"          %% "scalatest"                % "3.0.0" % "test"
)
        
