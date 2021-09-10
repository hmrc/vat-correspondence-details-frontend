/*
 * Copyright 2020 HM Revenue & Customs
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

import play.sbt.routes.RoutesKeys
import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "vat-correspondence-details-frontend"
val bootstrapPlayVersion       = "5.12.0"
val playFrontendHmrc           = "0.94.0-play-28"
val playPartialsVersion        = "8.2.0-play-28"
val authClientVersion          = "5.2.0-play-28"
val playLanguageVersion        = "5.1.0-play-28"
val scalaTestPlusVersion       = "5.1.0"
val scalatestVersion           = "3.1.4"
val pegdownVersion             = "1.6.0"
val jsoupVersion               = "1.14.1"
val mockitoVersion             = "3.2.9.0"
val scalaMockVersion           = "3.6.0"
val wiremockVersion            = "2.26.3"
val playJsonJodaVersion        = "2.9.2"
val flexmarkVersion            = "0.36.8"

lazy val appDependencies: Seq[ModuleID] = compile ++ test()
lazy val plugins: Seq[Plugins] = Seq.empty
lazy val playSettings: Seq[Setting[_]] = Seq.empty
RoutesKeys.routesImport := Seq.empty

lazy val coverageSettings: Seq[Setting[_]] = {
  import scoverage.ScoverageKeys

  val excludedPackages = Seq(
    "<empty>",
    ".*Reverse.*",
    ".*standardError*.*",
    ".*main_template*.*",
    "uk.gov.hmrc.BuildInfo",
    "app.*",
    "prod.*",
    "config.*",
    "testOnly.*",
    ".*feedback*.*",
    "views.*",
    "partials.*")

  Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 95,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

val compile = Seq(
  ws,
  "uk.gov.hmrc"       %% "bootstrap-frontend-play-28" % bootstrapPlayVersion,
  "uk.gov.hmrc"       %% "play-frontend-hmrc"         % playFrontendHmrc,
  "uk.gov.hmrc"       %% "play-partials"              % playPartialsVersion,
  "uk.gov.hmrc"       %% "auth-client"                % authClientVersion,
  "uk.gov.hmrc"       %% "play-language"              % playLanguageVersion,
  "com.typesafe.play" %% "play-json-joda"             % playJsonJodaVersion
)

def test(scope: String = "test, it"): Seq[ModuleID] = Seq(
  "org.scalatest"          %% "scalatest"                   % scalatestVersion      % scope,
  "org.scalatestplus.play" %% "scalatestplus-play"          % scalaTestPlusVersion  % scope,
  "org.scalatestplus"      %% "mockito-3-4"                 % mockitoVersion        % scope,
  "org.scalamock"          %% "scalamock-scalatest-support" % scalaMockVersion      % scope,
  "org.pegdown"            %  "pegdown"                     % pegdownVersion        % scope,
  "org.jsoup"              %  "jsoup"                       % jsoupVersion          % scope,
  "com.github.tomakehurst" %  "wiremock-jre8"               % wiremockVersion       % scope,
  "com.vladsch.flexmark"   %  "flexmark-all"                % flexmarkVersion       % scope
)

TwirlKeys.templateImports ++= Seq(
  "uk.gov.hmrc.govukfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.helpers._"
)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] = tests map {
  test =>
    Group(
      test.name,
      Seq(test),
      SubProcess(ForkOptions().withRunJVMOptions(Vector("-Dtest.name=" + test.name, "-Dlogger.resource=logback-test.xml")))
    )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(PlayKeys.playDefaultPort := 9148)
  .settings(coverageSettings: _*)
  .settings(playSettings: _*)
  .settings(majorVersion := 0)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    Keys.fork in Test := true,
    scalaVersion := "2.12.14",
    libraryDependencies ++= appDependencies,
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in IntegrationTest := false,
    unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest) (base => Seq(base / "it")).value,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    parallelExecution in IntegrationTest := false
  )
