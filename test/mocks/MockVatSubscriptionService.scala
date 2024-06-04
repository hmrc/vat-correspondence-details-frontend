/*
 * Copyright 2024 HM Revenue & Customs
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

import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import connectors.httpParsers.UpdateEmailHttpParser.UpdateEmailResponse
import connectors.httpParsers.UpdatePPOBHttpParser.UpdatePPOBResponse
import models.User
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterEach, Suite}
import services.VatSubscriptionService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockVatSubscriptionService extends MockFactory with BeforeAndAfterEach {
  this: Suite =>

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  val mockVatSubscriptionService: VatSubscriptionService = mock[VatSubscriptionService]

  def mockUpdateEmailAddress(vrn: String, email: String)(response: Future[UpdatePPOBResponse]): Unit =
    (mockVatSubscriptionService.updateEmail(_: String, _: String)(_: HeaderCarrier, _: ExecutionContext, _: User[_]))
      .expects(vrn, email, *, *, *)
      .returning(response)

  def mockUpdateContactPrefEmailAddress(vrn: String, email: String, response: Future[UpdateEmailResponse]): Unit =
    (mockVatSubscriptionService.updateContactPrefEmail(_: String, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(vrn, email, *, *)
      .returning(response)

  def mockUpdateWebsite(vrn: String, website: String)(response: Future[UpdatePPOBResponse]): Unit =
    (mockVatSubscriptionService.updateWebsite(_: String, _: String)(_: HeaderCarrier, _: ExecutionContext, _: User[_]))
      .expects(vrn, website, *, *, *)
      .returning(response)

  def mockUpdateLandlineNumber(vrn: String, landline: String)
                              (response: Future[UpdatePPOBResponse]): Unit =
    (mockVatSubscriptionService.updateLandlineNumber(_: String, _: String)(_: HeaderCarrier, _: ExecutionContext, _: User[_]))
      .expects(vrn, landline, *, *, *)
      .returning(response)

  def mockUpdateMobileNumber(vrn: String, mobile: String)
                            (response: Future[UpdatePPOBResponse]): Unit =
    (mockVatSubscriptionService.updateMobileNumber(_: String, _: String)(_: HeaderCarrier, _: ExecutionContext, _: User[_]))
      .expects(vrn, mobile, *, *, *)
      .returning(response)

  def mockUpdateContactPreference(vrn: String, contactPref: String)(response: Future[UpdatePPOBResponse]): Unit =
    (mockVatSubscriptionService.updateContactPreference(_: String, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(vrn, contactPref, *, *)
      .returning(response)

  def mockGetCustomerInfo(vrn: String)(response: GetCustomerInfoResponse): Unit =
    (mockVatSubscriptionService.getCustomerInfo(_: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(vrn, *, *)
      .returning(Future.successful(response))

  def mockUpdateEmail()(response: Future[UpdatePPOBResponse]): Unit =
    (mockVatSubscriptionService.updateEmail(_: String, _: String)(_: HeaderCarrier, _: ExecutionContext, _: User[_]))
      .expects(*, *, *, *, *)
      .returning(response)
}
