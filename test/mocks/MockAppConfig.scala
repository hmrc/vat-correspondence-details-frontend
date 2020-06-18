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

package mocks

import config.AppConfig
import config.features.Features
import play.api.{Configuration, Mode}
import play.api.i18n.Lang
import play.api.mvc.Call

class MockAppConfig(val runModeConfiguration: Configuration, val mode: Mode = Mode.Test) extends AppConfig {

  override def feedbackUrl(redirect: String): String = "localhost/feedback"
  override val appName = ""
  override val signInUrl = ""
  override val contactFormServiceIdentifier = ""
  override val contactFrontendService = ""
  override val assetsPrefix = ""
  override val feedbackFormPartialUrl = ""
  override val reportAProblemPartialUrl = ""
  override val reportAProblemNonJSUrl = ""
  override val agentServicesGovUkGuidance = "/setup-agent-services-account"
  override val hmrcPrivacyNoticeUrl = "hmrc-privacy-notice"
  override def feedbackSurveyUrl(identifier: String): String = s"/survey/$identifier"
  override def feedbackSignOutUrl(identifier: String): String = s"/sign-out/$identifier"
  override val unauthorisedSignOutUrl = "/sign-out"
  override val signInContinueUrl = ""
  override val govUkCommercialSoftware: String = "https://www.gov.uk/guidance/use-software-to-submit-your-vat-returns"
  override val vatAgentClientLookupServicePath: String = ""
  override val vatAgentClientLookupUnauthorised: String = "mockVaclfUnauthorised"
  override val features: Features = new Features(runModeConfiguration)
  override val emailVerificationBaseUrl: String = "mockEmailBaseUrl"
  override val manageVatSubscriptionServiceUrl: String = ""
  override val manageVatSubscriptionServicePath: String = "mockManageVatOverviewUrl"

  override def routeToSwitchLanguage: String => Call =
    (lang: String) => controllers.routes.LanguageController.switchToLanguage(lang)
  override def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  override val whitelistEnabled: Boolean = false
  override val whitelistedIps: Seq[String] = Seq("")
  override val whitelistExcludedPaths: Seq[Call] = Nil
  override val shutterPage: String = "https://www.tax.service.gov.uk/shutter/vat-through-software"

  override val vatSubscriptionHost: String = "mockVatSubscriptionHost"

  override val timeoutPeriod: Int = 999
  override val timeoutCountdown: Int = 999
  override val contactPreferencesService: String = ""
  override def contactPreferencesUrl(vrn: String): String = s"contact-preferences/vat/vrn/$vrn"
  override val contactHmrcUrl: String = "mockRemoveEmailUrl"

  override val accessibilityLinkUrl: String = "/vat-through-software/accessibility-statement"

  override val btaAccountDetailsUrl: String = "/bta-account-details"
}
