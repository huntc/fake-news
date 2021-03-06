// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `fake-news` =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaStreamTyped,
        library.akkaActorTestkitTyped % Test,
        library.scalaCheck            % Test,
        library.utest                 % Test
      )
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val akka       = "2.5.18"
      val scalaCheck = "1.14.0"
      val utest      = "0.6.6"
    }
    val akkaActorTestkitTyped = "com.typesafe.akka" %% "akka-actor-testkit-typed" % Version.akka
    val akkaStreamTyped =       "com.typesafe.akka" %% "akka-stream-typed"        % Version.akka

    val scalaCheck    = "org.scalacheck" %% "scalacheck"      % Version.scalaCheck
    val utest         = "com.lihaoyi"    %% "utest"           % Version.utest
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++
  scalafmtSettings

lazy val commonSettings =
  Seq(
    scalaVersion := "2.12.7",
    organization := "default",
    organizationName := "huntc",
    startYear := Some(2018),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding", "UTF-8",
      "-Ypartial-unification",
      "-Ywarn-unused-import"
    ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
    testFrameworks += new TestFramework("utest.runner.Framework")
)

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true
  )
