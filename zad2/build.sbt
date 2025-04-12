lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := "zad2",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "3.3.1",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-guice" % "2.9.0",
      "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test
    )
  )
