/*
 * Copyright 2014 Toshiyuki Takahashi
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

import sbt._
import sbt.Keys._

object SeedbedBuild extends Build {

  lazy val root = Project(
    id = "seedbed",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "seedbed",
      organization := "com.github.tototoshi",
      version := "0.1.0-SNAPSHOT",
      scalaVersion := "2.10.3",
      scalacOptions ++= Seq("-deprecation", "-language:_"),
      parallelExecution in Test := false,
      libraryDependencies ++= Seq(
        "com.h2database" % "h2" % "[1.3,)" % "test",
        "postgresql" % "postgresql" % "9.1-901-1.jdbc4" % "test",
        "org.scalatest" %% "scalatest" % "2.0" % "test"
      )
    ) ++ publishingSettings
  )

  val publishingSettings = Seq(
    publishMavenStyle := true,
    publishTo <<= version { (v: String) => _publishTo(v) },
    publishArtifact in Test := false,
    pomExtra := _pomExtra
  )

  def _publishTo(v: String) = {
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
    else Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }

  val _pomExtra =
    <url>http://github.com/tototoshi/seedbed</url>
      <licenses>
        <license>
          <name>Apache License, Version 2.0</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:tototoshi/seedbed.git</url>
        <connection>scm:git:git@github.com:tototoshi/seedbed.git</connection>
      </scm>
      <developers>
        <developer>
          <id>tototoshi</id>
          <name>Toshiyuki Takahashi</name>
          <url>http://tototoshi.github.io</url>
        </developer>
      </developers>

}
