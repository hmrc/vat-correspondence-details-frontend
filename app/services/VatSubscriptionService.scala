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

package services

import javax.inject.{Inject, Singleton}
import connectors.VatSubscriptionConnector
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import models.errors.{EmailAddressUpdateResponseModel, ErrorModel}
import play.api.Logger
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatSubscriptionService @Inject()(connector: VatSubscriptionConnector, emailVerificationService: EmailVerificationService) {

  def getCustomerInfo(vrn: String)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GetCustomerInfoResponse] =
    connector.getCustomerInfo(vrn)

  def updateEmailAddress(emailAddress: String, vrn: String)
                        (implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[Either[ErrorModel, EmailAddressUpdateResponseModel]] = {

  emailVerificationService.isEmailVerified(emailAddress) map {
      case Some(true) =>
        connector.updateEmailAddress(vrn, emailAddress) match {
          case Right(_) =>
            Right(EmailAddressUpdateResponseModel(true))
          case Left(error) =>
            Logger.warn(s"[VatSubscriptionService][UpdateEmailAddress] - Error received from vat-subscription: $error")
            Left(ErrorModel(NOT_FOUND, "Couldn't find a user with VRN provided"))
        }
      case Some(false) =>
        Logger.warn("[VatSubscriptionService][UpdateEmailAddress] - Email address not verified")
        Right(EmailAddressUpdateResponseModel(false))
      case None =>
        Logger.warn("[VatSubscriptionService][UpdateEmailAddress] - Couldn't verify email address")
        Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't verify email address"))
    }
  }
}
