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

package connectors

import common.SessionKeys
import config.AppConfig
import connectors.httpParsers.ResponseHttpParser.{HttpGetResult, HttpPutResult}
import javax.inject.{Inject, Singleton}
import models.User
import models.contactPreferences.ContactPreference
import models.customerInformation._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.LoggerUtil.{logDebug, logWarn}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatSubscriptionConnector @Inject()(http: HttpClient,
                                         appConfig: AppConfig) {

  private[connectors] def getCustomerInfoUrl(vrn: String): String =
    s"${appConfig.vatSubscriptionHost}/vat-subscription/$vrn/full-information"

  private[connectors] def updatePPOBUrl(vrn: String): String =
    s"${appConfig.vatSubscriptionHost}/vat-subscription/$vrn/ppob"

  private[connectors] def updateContactPreferenceUrl(vrn: String): String =
    s"${appConfig.vatSubscriptionHost}/vat-subscription/$vrn/contact-preference"

  def getCustomerInfo(vrn: String)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpGetResult[CustomerInformation]] = {

    import connectors.httpParsers.GetCustomerInfoHttpParser.CustomerInfoReads

    http.GET[HttpGetResult[CustomerInformation]](getCustomerInfoUrl(vrn)).map {
      case customerInfo@Right(_) =>
        logDebug(s"[VatSubscriptionConnector][getCustomerInfo] successfully received customer info response")
        customerInfo
      case httpError@Left(error) =>
        logWarn("[VatSubscriptionConnector][getCustomerInfo] received error - " + error.message)
        httpError
    }
  }

  def updatePPOB(vrn: String, ppob: PPOB)
                (implicit hc: HeaderCarrier,
                 ec: ExecutionContext,
                 user: User[_]): Future[HttpPutResult[UpdatePPOBSuccess]] = {

    import connectors.httpParsers.UpdatePPOBHttpParser.UpdatePPOBReads

    val updateModel = UpdatePPOB(
      ppob.address, ppob.contactDetails, ppob.websiteAddress, user.session.get(SessionKeys.verifiedAgentEmail)
    )

    http.PUT[UpdatePPOB, HttpPutResult[UpdatePPOBSuccess]](updatePPOBUrl(vrn), updateModel).map {
      case result@Right(_) =>
        result
      case httpError@Left(error) =>
        logWarn("[VatSubscriptionConnector][updatePPOB] received error - " + error.message)
        httpError
    }
  }

  def updateContactPreference(vrn: String, contactPreference: String)
                             (implicit hc: HeaderCarrier,
                              ec: ExecutionContext,
                              user: User[_]): Future[HttpPutResult[UpdatePPOBSuccess]] = {

    import connectors.httpParsers.UpdatePPOBHttpParser.UpdatePPOBReads

    val updateModel = ContactPreference(contactPreference)

    http.PUT[ContactPreference, HttpPutResult[UpdatePPOBSuccess]](updateContactPreferenceUrl(vrn), updateModel).map {
      case result@Right(_) =>
        result
      case httpError@Left(error) =>
        logWarn("[VatSubscriptionConnector][updateContactPreference] received error - " + error.message)
        httpError
    }
  }
}
