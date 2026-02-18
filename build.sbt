scalaVersion := "3.3.7"

name := "zio-sandbox"
organization := "com.example"
version := "0.1.0"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio-blocks-schema" % "0.0.14+30-89084a1a+20260218-1703-SNAPSHOT",
  "dev.zio" %% "zio-blocks-schema-toon" % "0.0.14+30-89084a1a+20260218-1703-SNAPSHOT",
  "dev.zio" %% "zio-blocks-schema-iron" % "0.0.14+30-89084a1a+20260218-1703-SNAPSHOT",
  "org.jsoup" % "jsoup" % "1.22.1",
  "io.github.iltotore" %% "iron" % "3.2.3"
)
