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
import connectors.VatSubscriptionConnector
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import connectors.httpParsers.ResponseHttpParser.HttpResult
import connectors.httpParsers.UpdatePPOBHttpParser.UpdatePPOBResponse
import javax.inject.{Inject, Singleton}
import models.User
import models.customerInformation.{ContactDetails, PPOB, UpdateEmailSuccess, UpdatePPOBSuccess}
import models.errors.ErrorModel
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatSubscriptionService @Inject()(connector: VatSubscriptionConnector, emailVerificationService: EmailVerificationService)
                                      (implicit val appConfig: AppConfig) {

  private[services] def buildEmailUpdateModel(email: String, ppob: PPOB): PPOB = {
    val existingContactDetails: ContactDetails =
      ppob.contactDetails.getOrElse(ContactDetails(None, None, None, None, None))
    ppob.copy(
      contactDetails = Some(existingContactDetails.copy(emailAddress = Some(email), emailVerified = Some(true)))
    )
  }

  private[services] def buildLandlineUpdateModel(landline: String, ppob: PPOB): PPOB = {
    val existingContactDetails: ContactDetails =
      ppob.contactDetails.getOrElse(ContactDetails(None, None, None, None, None))
    ppob.copy(
      contactDetails = Some(existingContactDetails.copy(phoneNumber = Some(landline).filter(_.nonEmpty)))
    )
  }

  private[services] def buildMobileUpdateModel(mobile: String, ppob: PPOB): PPOB = {
    val existingContactDetails: ContactDetails =
      ppob.contactDetails.getOrElse(ContactDetails(None, None, None, None, None))
    ppob.copy(
      contactDetails = Some(existingContactDetails.copy(mobileNumber = Some(mobile).filter(_.nonEmpty)))
    )
  }

  private[services] def buildWebsiteUpdateModel(website: String, ppob: PPOB): PPOB =
    if(website.nonEmpty) ppob.copy(websiteAddress = Some(website)) else ppob.copy(websiteAddress = None)


  def getCustomerInfo(vrn: String)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GetCustomerInfoResponse] =
    connector.getCustomerInfo(vrn)

  def updateEmail(vrn: String, email: String)
                 (implicit headerCarrier: HeaderCarrier, ec: ExecutionContext, user: User[_]): Future[UpdatePPOBResponse] =

    emailVerificationService.isEmailVerified(email).flatMap {
      case Some(true) =>
        this.getCustomerInfo(vrn) flatMap {
          case Right(customerInfo) => connector.updatePPOB(vrn, buildEmailUpdateModel(email, customerInfo.ppob))
          case Left(error) => Future.successful(Left(error))
        }
      case Some(false) => Future.successful(Right(UpdatePPOBSuccess("")))
      case None => Future.successful(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't verify email address")))
    }

  def updateContactPrefEmail(vrn: String, email: String)
                            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResult[UpdateEmailSuccess]] =
    connector.updateEmail(vrn, email)

  def updateWebsite(vrn: String, website: String)
                   (implicit hc: HeaderCarrier, ec: ExecutionContext, user: User[_]): Future[UpdatePPOBResponse] =
    connector.getCustomerInfo(vrn).flatMap {
      case Right(customerInfo) => connector.updatePPOB(vrn, buildWebsiteUpdateModel(website, customerInfo.ppob))
      case Left(error) => Future.successful(Left(error))
    }

  def updateLandlineNumber(vrn: String, landline: String)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext, user: User[_]): Future[UpdatePPOBResponse] =
    connector.getCustomerInfo(vrn).flatMap {
      case Right(customerInfo) =>
        connector.updatePPOB(vrn, buildLandlineUpdateModel(landline, customerInfo.ppob))
      case Left(error) => Future.successful(Left(error))
    }

  def updateMobileNumber(vrn: String, mobile: String)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext, user: User[_]): Future[UpdatePPOBResponse] =
    connector.getCustomerInfo(vrn).flatMap {
      case Right(customerInfo) =>
        connector.updatePPOB(vrn, buildMobileUpdateModel(mobile, customerInfo.ppob))
      case Left(error) => Future.successful(Left(error))
    }

  def updateContactPreference(vrn: String, contactPreference: String)
                             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[UpdatePPOBResponse] =
        connector.updateContactPreference(vrn, contactPreference)

}
