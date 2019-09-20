/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.i18n.Lang
import play.api.mvc.Call
import play.api.Configuration
import uk.gov.hmrc.play.binders.ContinueUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

trait AppConfig {
  val appName: String
  val assetsPrefix: String
  val analyticsToken: String
  val analyticsHost: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val agentServicesGovUkGuidance: String
  def feedbackSurveyUrl(identifier: String): String
  def feedbackSignOutUrl(identifier: String): String
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
  val vatAgentClientLookupServicePath: String
  val features: Features
  val emailVerificationBaseUrl: String
  val vatSubscriptionHost: String
  val manageVatSubscriptionServiceUrl: String
  val manageVatSubscriptionServicePath: String
  val feedbackFormPartialUrl: String
  val contactFrontendService: String
  val contactFormServiceIdentifier: String
  val timeoutPeriod: Int
  val timeoutCountdown: Int
  val contactPreferencesService: String
  val contactHmrcUrl: String
  def contactPreferencesUrl(vrn: String): String
  def feedbackUrl(redirect: String): String
  val accessibilityLinkUrl: String
}

@Singleton
class FrontendAppConfig @Inject()(configuration: Configuration, sc: ServicesConfig) extends AppConfig {

  override lazy val appName: String = sc.getString("appName")

  override lazy val contactFormServiceIdentifier = "VATC"
  override lazy val contactFrontendService: String = sc.getString(Keys.contactFrontendService)
  override lazy val feedbackFormPartialUrl: String = s"$contactFrontendService/contact/beta-feedback/form"
  override lazy val reportAProblemPartialUrl = s"$contactFrontendService/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"$contactFrontendService/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  override lazy val assetsPrefix: String = sc.getString(Keys.assetsUrl) + sc.getString(Keys.assetsVersion)

  override lazy val analyticsToken: String = sc.getString(Keys.googleAnalyticsToken)
  override lazy val analyticsHost: String = sc.getString(Keys.googleAnalyticsHost)

  override lazy val agentServicesGovUkGuidance: String = sc.getString(Keys.govUkSetupAgentServices)

  private lazy val governmentGatewayHost: String = sc.getString(Keys.governmentGatewayHost)

  override val features = new Features(configuration)

  private lazy val feedbackSurveyBase = sc.getString(Keys.surveyUrl) + sc.getString(Keys.surveyPath)
  override def feedbackSurveyUrl(identifier: String): String = s"$feedbackSurveyBase/$identifier"

  private lazy val signInBaseUrl: String = sc.getString(Keys.signInBaseUrl)
  private lazy val signInOrigin = sc.getString("appName")
  override lazy val signInUrl: String = s"$signInBaseUrl?continue=$signInContinueUrl&origin=$signInOrigin"
  override lazy val signInContinueUrl: String = ContinueUrl(manageVatSubscriptionServicePath).encodedUrl
  override def feedbackSignOutUrl(identifier: String): String =
    s"$governmentGatewayHost/gg/sign-out?continue=${feedbackSurveyUrl(identifier)}"
  override lazy val unauthorisedSignOutUrl: String = s"$governmentGatewayHost/gg/sign-out?continue=$signInContinueUrl"

  override lazy val emailVerificationBaseUrl: String = sc.baseUrl(Keys.emailVerificationBaseUrl)

  override def routeToSwitchLanguage: String => Call = (lang: String) => controllers.routes.LanguageController.switchToLanguage(lang)
  override def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  private def whitelistConfig(key: String): Seq[String] = Some(new String(Base64.getDecoder
    .decode(sc.getString(key)), "UTF-8"))
    .map(_.split(",")).getOrElse(Array.empty).toSeq

  override lazy val whitelistEnabled: Boolean = sc.getBoolean(Keys.whitelistEnabled)
  override lazy val whitelistedIps: Seq[String] = whitelistConfig(Keys.whitelistedIps)
  override lazy val whitelistExcludedPaths: Seq[Call] = whitelistConfig(Keys.whitelistExcludedPaths).map(path => Call("GET", path))
  override lazy val shutterPage: String = sc.getString(Keys.whitelistShutterPage)

  override lazy val agentInvitationsFastTrack: String = sc.getString(Keys.agentInvitationsFastTrack)
  override lazy val govUkCommercialSoftware: String = sc.getString(Keys.govUkCommercialSoftware)

  private lazy val host: String = sc.getString(Keys.host)

  private val vatAgentClientLookupServiceUrl: String = sc.getString(Keys.vatAgentClientLookupServiceUrl)
  override val vatAgentClientLookupServicePath: String =
    vatAgentClientLookupServiceUrl + sc.getString(Keys.vatAgentClientLookupServicePath)
  override val vatSubscriptionHost: String = sc.baseUrl(Keys.vatSubscription)

  override val manageVatSubscriptionServiceUrl: String = sc.getString(Keys.manageVatSubscriptionServiceUrl)
  override val manageVatSubscriptionServicePath: String =
    manageVatSubscriptionServiceUrl + sc.getString(Keys.manageVatSubscriptionServicePath)

  override lazy val timeoutPeriod: Int = sc.getInt(Keys.timeoutPeriod)
  override lazy val timeoutCountdown: Int = sc.getInt(Keys.timeoutCountdown)


  override val contactHmrcUrl: String = sc.getString(Keys.contactHmrc)

  override lazy val contactPreferencesService: String = {
    if(features.stubContactPreferences()){
      sc.baseUrl("vat-subscription-dynamic-stub")
    } else {
      sc.baseUrl(Keys.contactPreferencesService)
    }
  }

  override def contactPreferencesUrl(vrn: String): String = contactPreferencesService + s"/contact-preferences/vat/vrn/$vrn"

  override def feedbackUrl(redirect: String): String = s"$contactFrontendService/contact/beta-feedback?service=$contactFormServiceIdentifier" +
    s"&backUrl=${ContinueUrl(host + redirect).encodedUrl}"

  override val accessibilityLinkUrl: String = sc.getString(ConfigKeys.vatSummaryFrontendServiceUrl) + sc.getString(ConfigKeys.vatSummaryAccessibilityUrl)

}
