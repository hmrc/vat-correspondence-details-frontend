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

package connectors

import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import common.EmailVerificationKeys._
import connectors.httpParsers.CreateEmailVerificationRequestHttpParser.CreateEmailVerificationRequestResponse
import connectors.httpParsers.GetEmailVerificationStateHttpParser.GetEmailVerificationStateResponse
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

@Singleton
class EmailVerificationConnector @Inject()(http: HttpClient,
                                           appConfig: AppConfig) {

  private[connectors] def checkVerifiedEmailUrl(email: String): String =
    s"${appConfig.emailVerificationBaseUrl}/email-verification/verified-email-addresses/$email"

  private[connectors] lazy val createEmailVerificationRequestUrl: String =
    s"${appConfig.emailVerificationBaseUrl}/email-verification/verification-requests"

  def getEmailVerificationState(emailAddress: String)
                               (implicit hc: HeaderCarrier): Future[GetEmailVerificationStateResponse] =
    http.GET[GetEmailVerificationStateResponse](checkVerifiedEmailUrl(emailAddress))

  def createEmailVerificationRequest(emailAddress: String, continueUrl: String)
                                    (implicit hc: HeaderCarrier): Future[CreateEmailVerificationRequestResponse] = {
    val jsonBody =
      Json.obj(
        EmailKey -> emailAddress,
        TemplateIdKey -> "verifyEmailAddress",
        TemplateParametersKey -> Json.obj(),
        LinkExpiryDurationKey -> "P1D",
        ContinueUrlKey -> continueUrl
      )

    http.POST[JsObject, CreateEmailVerificationRequestResponse](createEmailVerificationRequestUrl, jsonBody)
  }
}