name := "sconduit"

organization := "me.elrod"

version := "1.0.0-SNAPSHOT"

scalacOptions ++= Seq("-deprecation")

libraryDependencies += "org.scalatest" % "scalatest_2.10.0-RC3" % "1.8-B1" % "test"

libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.9.5"

libraryDependencies += "net.databinder.dispatch" %% "json4s-jackson" % "0.9.5"