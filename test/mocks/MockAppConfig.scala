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

package mocks

import config.AppConfig
import play.api.{Configuration, Mode}
import play.api.Mode.Mode
import play.api.i18n.Lang
import play.api.mvc.Call

class MockAppConfig(val runModeConfiguration: Configuration, val mode: Mode = Mode.Test) extends AppConfig {
  override val signInUrl = ""
  override val contactHost = ""
  override val assetsPrefix = ""
  override val analyticsToken = ""
  override val analyticsHost = ""
  override val reportAProblemPartialUrl = ""
  override val reportAProblemNonJSUrl = ""
  override val agentServicesGovUkGuidance = "/setup-agent-services-account"
  override val unauthorisedSignOutUrl = "/sign-out"
  override val signInContinueUrl = ""
  override val agentInvitationsFastTrack: String = "/agent-invitations-frontend"
  override val host = ""
  override val govUkCommercialSoftware: String = ""
  override val vatAgentClientLookupServicePath: String = ""
  override val vatAgentClientLookupServiceUrl: String = ""

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

}
