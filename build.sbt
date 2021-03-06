import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
import sbtunidoc.Plugin.UnidocKeys._
import ScoverageSbtPlugin._

lazy val buildSettings = Seq(
  organization := "com.github.finagle",
  scalaVersion := "2.11.7",
  crossScalaVersions := Seq("2.10.6", "2.11.7")
)

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-language:implicitConversions",
  "-Yinline-warnings",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard",
  "-Ypatmat-exhaust-depth", "off",
  "-Xfuture"
)

lazy val specs2Version = "3.7"

lazy val testDependencies = Seq(
  "org.specs2"      %%  "specs2-core"       %   specs2Version,
  "org.specs2"      %%  "specs2-scalacheck" %   specs2Version,
  "org.specs2"      %%  "specs2-junit"      %   specs2Version,
  "org.specs2"      %%  "specs2-mock"       %   specs2Version 
)

scalacOptions in Test ++= Seq("-Yrangepos")

lazy val baseSettings = Seq(
  scalacOptions ++= compilerOptions, 
  scalacOptions in (Compile, console) := compilerOptions, 
  scalacOptions in (Compile, doc) ++= Seq(
    "-doc-title", "Roc",
    "-doc-version", version.value,
    "-groups"
  ),
  libraryDependencies ++= testDependencies.map(_ % "test"),
  resolvers += Resolver.sonatypeRepo("snapshots"),
  coverageEnabled := true,
  autoAPIMappings := true,
  resolvers += "Twitter Maven repo" at "http://maven.twttr.com/"
)

//enablePlugins(SiteScaladocPlugin)

lazy val allSettings = buildSettings ++ baseSettings 

lazy val coreVersion = "0.0.1-SNAPSHOT"

lazy val catsVersion = "0.4.1"

lazy val finagleVersion = "6.33.0"

lazy val nettyVersion = "4.1.0.CR2"

lazy val roc = project.in(file("."))
  .settings(moduleName := "root")
  .settings(allSettings)
  .settings(docSettings)
  .aggregate(core)
  .dependsOn(core)

lazy val core =  project
  .settings(moduleName := "roc-core")
  .settings(version := coreVersion)
  .settings(allSettings:_*)
  .settings(docSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel"   %%  "cats"          %  catsVersion,
      "org.spire-math"  %%  "algebra"       %  "0.3.1",
      "io.netty"        %   "netty-buffer"  %  nettyVersion,
      "com.twitter"     %%  "finagle-core"  %  finagleVersion
    )
  )

lazy val tagName = Def.setting{
 s"v${if (releaseUseGlobalVersion.value) (version in ThisBuild).value else version.value}"
}

lazy val sharedPublishSettings = Seq(
  releaseCrossBuild := true,
  releaseTagName := tagName.value,
  licenses := Seq("BSD New" -> url("https://opensource.org/licenses/BSD-3-Clause")),
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := Function.const(false),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("Snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("Releases" at nexus + "service/local/staging/deploy/maven2")
  },
  autoAPIMappings := true,
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/finagle/roc"),
      "scm:git:git@github.com:finagle/roc.git"
    )
  ),
  pomExtra := (
    <developers>
      <developer>
        <id>penland365</id>
        <name>Jeffrey Davis</name>
        <url>https://twitter.com/penland365</url>
      </developer>
    </developers>
  )
)

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

lazy val docSettings = site.settings ++ ghpages.settings ++ unidocSettings ++ Seq(
  autoAPIMappings := true,
  ghpagesNoJekyll := false,
  site.addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), "docs"),
  git.remoteRepo := "git@github.com:finagle/roc.git",
  unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject
)
