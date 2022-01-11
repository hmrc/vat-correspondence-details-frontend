/*
 * Copyright 2022 HM Revenue & Customs
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

object ConfigKeys {
  val assetsUrl: String = "assets.url"
  val assetsVersion: String = "assets.version"

  val contactFrontendService: String = "contact-frontend.url"

  val govUkSetupAgentServices: String = "govuk.guidance.setupAgentServices.url"
  val hmrcPrivacyNoticeUrl: String = "govuk.guidance.hmrcPrivacyNotice.url"
  val govUkCommercialSoftware: String = "govuk.software.commercialSoftware.url"

  val governmentGatewayHost: String = "government-gateway.host"

  val signInBaseUrl: String = "signIn.url"

  val vatAgentClientLookupServiceUrl: String = "vat-agent-client-lookup-frontend.url"
  val vatAgentClientLookupServicePath: String = "vat-agent-client-lookup-frontend.path"
  val vatAgentClientLookupAgentHubPath: String = "vat-agent-client-lookup-frontend.agent-hub"
  val vatAgentClientLookupServiceUnauthPath: String = "vat-agent-client-lookup-frontend.unauthorised"
  val manageVatSubscriptionServiceUrl: String = "manage-vat-subscription-frontend.url"
  val manageVatSubscriptionServicePath: String = "manage-vat-subscription-frontend.path"

  val emailVerificationBaseUrl: String = "email-verification"

  val host: String = "host"

  val emailVerificationFeature: String = "features.emailVerification.enabled"

  val vatSubscription: String = "vat-subscription"

  val surveyUrl: String = "feedback-frontend.url"
  val surveyPath: String = "feedback-frontend.path"

  val timeoutPeriod: String = "timeout.period"
  val timeoutCountdown: String = "timeout.countdown"

  val contactHmrc: String = "contact-hmrc.url"

  val vatSummaryFrontendServiceUrl: String = "vat-summary-frontend.url"
  val vatSummaryAccessibilityUrl: String = "vat-summary-frontend.accessibilityUrl"
  val vatSummaryFrontendOverviewUrl: String = "vat-summary-frontend.vatOverviewUrl"

  val btaHost: String = "business-tax-account.url"
  val btaHomeUrl: String = "business-tax-account.homeUrl"
  val btaAccountDetailsUrl: String = "business-tax-account.accountDetails"

  val gtmContainer: String = "tracking-consent-frontend.gtm.container"
}