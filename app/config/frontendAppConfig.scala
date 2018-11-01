/*
 * Copyright 2018 HM Revenue & Customs
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

package config

import java.util.Base64

import config.features.Features
import config.{ConfigKeys => Keys}
import javax.inject.{Inject, Singleton}
import play.api.Mode.Mode
import play.api.i18n.Lang
import play.api.mvc.Call
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.binders.ContinueUrl
import uk.gov.hmrc.play.config.ServicesConfig

trait AppConfig extends ServicesConfig {
  val appName: String
  val assetsPrefix: String
  val analyticsToken: String
  val analyticsHost: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val agentServicesGovUkGuidance: String
  val unauthorisedSignOutUrl: String
  def routeToSwitchLanguage: String => Call
  def languageMap: Map[String, Lang]
  val whitelistEnabled: Boolean
  val whitelistedIps: Seq[String]
  val whitelistExcludedPaths: Seq[Call]
  val shutterPage: String
  val signInUrl: String
  val signInContinueUrl: String
  val agentInvitationsFastTrack: String
  val govUkCommercialSoftware: String
  val host: String
  val vatAgentClientLookupServiceUrl: String
  val vatAgentClientLookupServicePath: String
  val features: Features
  val emailVerificationBaseUrl: String
  val vatSubscriptionHost: String
  val manageVatSubscriptionServiceUrl: String
  val manageVatSubscriptionServicePath: String
  val feedbackFormPartialUrl: String
  val contactFrontendService: String
  val contactFormServiceIdentifier: String
}

@Singleton
class FrontendAppConfig @Inject()(val runModeConfiguration: Configuration, environment: Environment) extends AppConfig {

  override protected def mode: Mode = environment.mode
  override lazy val appName: String = getString("appName")

  override lazy val contactFormServiceIdentifier = "VATC"
  override lazy val contactFrontendService: String = baseUrl(Keys.contactFrontendService)
  override lazy val feedbackFormPartialUrl: String = s"$contactFrontendService/contact/beta-feedback/form"
  override lazy val reportAProblemPartialUrl = s"$contactFrontendService/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"$contactFrontendService/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  override lazy val assetsPrefix: String = getString(Keys.assetsUrl) + getString(Keys.assetsVersion)

  override lazy val analyticsToken: String = getString(Keys.googleAnalyticsToken)
  override lazy val analyticsHost: String = getString(Keys.googleAnalyticsHost)

  override lazy val agentServicesGovUkGuidance: String = getString(Keys.govUkSetupAgentServices)

  private lazy val governmentGatewayHost: String = getString(Keys.governmentGatewayHost)

  override val features = new Features(runModeConfiguration)

  private lazy val signInBaseUrl: String = getString(Keys.signInBaseUrl)
  private lazy val signInOrigin = getString("appName")
  override lazy val signInUrl: String = s"$signInBaseUrl?continue=$signInContinueUrl&origin=$signInOrigin"
  override lazy val signInContinueUrl: String = ContinueUrl(manageVatSubscriptionServicePath).encodedUrl
  override lazy val unauthorisedSignOutUrl: String = s"$governmentGatewayHost/gg/sign-out?continue=$signInContinueUrl"

  override lazy val emailVerificationBaseUrl: String = baseUrl(Keys.emailVerificationBaseUrl)

  override def routeToSwitchLanguage: String => Call = (lang: String) => controllers.routes.LanguageController.switchToLanguage(lang)
  override def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  private def whitelistConfig(key: String): Seq[String] = Some(new String(Base64.getDecoder
    .decode(getString(key)), "UTF-8"))
    .map(_.split(",")).getOrElse(Array.empty).toSeq

  override lazy val whitelistEnabled: Boolean = getBoolean(Keys.whitelistEnabled)
  override lazy val whitelistedIps: Seq[String] = whitelistConfig(Keys.whitelistedIps)
  override lazy val whitelistExcludedPaths: Seq[Call] = whitelistConfig(Keys.whitelistExcludedPaths).map(path => Call("GET", path))
  override lazy val shutterPage: String = getString(Keys.whitelistShutterPage)

  override lazy val agentInvitationsFastTrack: String = getString(Keys.agentInvitationsFastTrack)
  override lazy val govUkCommercialSoftware: String = getString(Keys.govUkCommercialSoftware)

  override lazy val host: String = getString(Keys.host)

  override val vatAgentClientLookupServiceUrl: String = getString(Keys.vatAgentClientLookupServiceUrl)
  override val vatAgentClientLookupServicePath: String = getString(Keys.vatAgentClientLookupServicePath)
  override val vatSubscriptionHost: String = baseUrl(Keys.vatSubscription)

  override val manageVatSubscriptionServiceUrl: String = getString(Keys.manageVatSubscriptionServiceUrl)
  override val manageVatSubscriptionServicePath: String =
    manageVatSubscriptionServiceUrl + getString(Keys.manageVatSubscriptionServicePath)
}
