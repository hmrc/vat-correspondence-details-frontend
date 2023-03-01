/*
 * Copyright 2023 HM Revenue & Customs
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

package services

import config.AppConfig
import connectors.EmailVerificationConnector
import connectors.httpParsers.CreateEmailVerificationRequestHttpParser.{EmailAlreadyVerified, EmailVerificationRequestSent}
import connectors.httpParsers.GetEmailVerificationStateHttpParser.{EmailNotVerified, EmailVerified}
import connectors.httpParsers.RequestPasscodeHttpParser.{EmailIsAlreadyVerified, EmailVerificationPasscodeRequestSent}
import connectors.httpParsers.ResponseHttpParser.HttpResult
import connectors.httpParsers.VerifyPasscodeHttpParser.VerifyPasscodeRequest
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailVerificationService @Inject()(emailVerificationConnector: EmailVerificationConnector,
                                         appConfig: AppConfig)
                                        (implicit ec: ExecutionContext) {

  def isEmailVerified(email: String)(implicit hc: HeaderCarrier): Future[Option[Boolean]] =

    if(appConfig.features.emailVerificationEnabled()) {
      emailVerificationConnector.getEmailVerificationState(email) map {
        case Right(EmailVerified) =>
          Some(true)
        case Right(EmailNotVerified) =>
          Some(false)
        case Left(_) =>
          None
      }
    } else {
      Future.successful(Some(true))
    }

  def createEmailVerificationRequest(email: String, continueUrl: String)
                                    (implicit hc: HeaderCarrier): Future[Option[Boolean]] =

    if(appConfig.features.emailVerificationEnabled()) {
      emailVerificationConnector.createEmailVerificationRequest(email, continueUrl) map {
        case Right(EmailVerificationRequestSent) =>
          Some(true)
        case Right(EmailAlreadyVerified) =>
          Some(false)
        case _ =>
          None
      }
    } else {
      Future.successful(Some(false))
    }

  def createEmailPasscodeRequest(email: String, lang: String)(implicit hc: HeaderCarrier): Future[Option[Boolean]] =

    emailVerificationConnector.requestEmailPasscode(email, lang) map {
      case Right(EmailVerificationPasscodeRequestSent) => Some(true)
      case Right(EmailIsAlreadyVerified) => Some(false)
      case _ => None
    }

  def verifyPasscode(email: String, passcode: String)
                    (implicit hc: HeaderCarrier): Future[HttpResult[VerifyPasscodeRequest]] =
    emailVerificationConnector.verifyPasscode(email, passcode)
}
