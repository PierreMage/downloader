name := "downloader"

organization := "com.github.pierremage"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.6"

crossScalaVersions := Seq("2.11.6", "2.10.4")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

libraryDependencies ++= {
  Seq(
    "org.apache.commons" % "commons-vfs2" % "2.0",
    "commons-httpclient" % "commons-httpclient" % "3.1",
    "com.jcraft" % "jsch" % "0.1.53",
    "org.slf4j" % "slf4j-api" % "1.7.10",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "org.scalatest" %% "scalatest" % "2.2.5" % Test,
    "org.mockftpserver" % "MockFtpServer" % "2.6" % Test
  )
}
