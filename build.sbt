/*
 * Copyright 2019 BusyMachines
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//=============================================================================
//============================== build details ================================
//=============================================================================

addCommandAlias("github-gen", "githubWorkflowGenerate")
addCommandAlias("github-check", "githubWorkflowCheck")
addCommandAlias("run-it", "IntegrationTest/test")
Global / onChangedBuildSource := ReloadOnSourceChanges

val Scala213  = "2.13.5"
val Scala3RC1 = "3.0.0-RC1"

//=============================================================================
//============================ publishing details =============================
//=============================================================================

//see: https://github.com/xerial/sbt-sonatype#buildsbt
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

ThisBuild / baseVersion  := "0.3"
ThisBuild / organization := "com.busymachines"
ThisBuild / organizationName := "BusyMachines"
ThisBuild / homepage     := Option(url("https://github.com/busymachines/pureharm-db-testkit"))

ThisBuild / scmInfo := Option(
  ScmInfo(
    browseUrl  = url("https://github.com/busymachines/pureharm-db-testkit"),
    connection = "git@github.com:busymachines/pureharm-db-core.git",
  )
)

/** I want my email. So I put this here. To reduce a few lines of code,
  * the sbt-spiewak plugin generates this (except email) from these two settings:
  * {{{
  * ThisBuild / publishFullName   := "Loránd Szakács"
  * ThisBuild / publishGithubUser := "lorandszakacs"
  * }}}
  */
ThisBuild / developers := List(
  Developer(
    id    = "lorandszakacs",
    name  = "Loránd Szakács",
    email = "lorand.szakacs@protonmail.com",
    url   = new java.net.URL("https://github.com/lorandszakacs"),
  )
)

ThisBuild / startYear := Some(2019)
ThisBuild / licenses   := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

//until we get to 1.0.0, we keep strictSemVer false
ThisBuild / strictSemVer              := false
ThisBuild / spiewakCiReleaseSnapshots := false
ThisBuild / spiewakMainBranches       := List("main")
ThisBuild / Test / publishArtifact    := false

ThisBuild / scalaVersion       := Scala213
ThisBuild / crossScalaVersions := List(Scala213) //List(Scala213, Scala3RC1)

//required for binary compat checks
ThisBuild / versionIntroduced := Map(
  Scala213  -> "0.1.0",
  Scala3RC1 -> "0.1.0",
)

//=============================================================================
//================================ Dependencies ===============================
//=============================================================================
ThisBuild / resolvers += Resolver.sonatypeRepo("releases")
ThisBuild / resolvers += Resolver.sonatypeRepo("snapshots")

// format: off
val pureharmCoreV           = "0.3.0"       //https://github.com/busymachines/pureharm-core/releases
val pureharmEffectsV        = "0.5.0"       //https://github.com/busymachines/pureharm-effects-cats/releases
val pureharmDBCoreV         = "0.5.0"       //https://github.com/busymachines/pureharm-db-core/releases
val pureharmDBCoreJDBCV     = "0.6.0"       //https://github.com/busymachines/pureharm-db-core-jdbc/releases
val pureharmJSONCirceV      = "0.3.0-M1"    //https://github.com/busymachines/pureharm-json-circe/releases
val pureharmDBTestkitV      = "0.3.0"       //https://github.com/busymachines/pureharm-db-testkit/releases
val slickV                  = "3.4.0-M1"    //https://github.com/slick/slick/releases
val hikariCPV               = "5.0.1"       //java — https://github.com/brettwooldridge/HikariCP/releases
val log4catsV               = "1.4.0"       //https://github.com/typelevel/log4cats/releases
// format: on

//=============================================================================
//============================== Project details ==============================
//=============================================================================

lazy val root = project
  .in(file("."))
  .aggregate(
    `db-slick`,
    `db-testkit-slick`,
  )
  .enablePlugins(NoPublishPlugin)
  .enablePlugins(SonatypeCiReleasePlugin)
  .settings(commonSettings)

lazy val `db-slick` = project
  .settings(commonSettings)
  .settings(
    name := "pureharm-db-slick",
    libraryDependencies ++= Seq(
      // format: off
      "com.busymachines"      %% "pureharm-core-identifiable"   % pureharmCoreV         withSources(),
      "com.busymachines"      %% "pureharm-core-anomaly"        % pureharmCoreV         withSources(),
      "com.busymachines"      %% "pureharm-core-sprout"         % pureharmCoreV         withSources(),
      "com.busymachines"      %% "pureharm-effects-cats"        % pureharmEffectsV      withSources(),
      "com.busymachines"      %% "pureharm-db-core"             % pureharmDBCoreV       withSources(),
      "com.busymachines"      %% "pureharm-db-core-jdbc"        % pureharmDBCoreJDBCV   withSources(),
      "com.busymachines"      %% "pureharm-json-circe"          % pureharmJSONCirceV    withSources(),
      "com.typesafe.slick"    %% "slick"                        % slickV                withSources(),
      "com.zaxxer"             % "HikariCP"                     % hikariCPV             withSources(),
      // format: on
    )
  ).settings(
    javaOptions ++= Seq("-source", "1.8", "-target", "1.8")
  )

lazy val `db-testkit-slick` = project
  .settings(commonSettings)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(
    name := "pureharm-db-testkit-slick",
    libraryDependencies ++= Seq(
      // format: off
      "com.busymachines"    %% "pureharm-db-testkit"      % pureharmDBTestkitV              withSources(),
      "com.busymachines"    %% "pureharm-db-test-data"    % pureharmDBTestkitV % "it,test"  withSources(),
      "org.typelevel"       %% "log4cats-slf4j"           % log4catsV          % "it,test"  withSources(),
      // format: on
    )
  ).settings(
    javaOptions ++= Seq("-source", "1.8", "-target", "1.8")
  ).dependsOn(
    `db-slick`
  )

//=============================================================================
//================================= Settings ==================================
//=============================================================================
lazy val commonSettings = Seq(
  Compile / unmanagedSourceDirectories ++= {
    val major = if (isDotty.value) "-3" else "-2"
    List(CrossType.Pure, CrossType.Full).flatMap(
      _.sharedSrcDir(baseDirectory.value, "main").toList.map(f => file(f.getPath + major))
    )
  },
  Test / unmanagedSourceDirectories ++= {
    val major = if (isDotty.value) "-3" else "-2"
    List(CrossType.Pure, CrossType.Full).flatMap(
      _.sharedSrcDir(baseDirectory.value, "test").toList.map(f => file(f.getPath + major))
    )
  },
)
