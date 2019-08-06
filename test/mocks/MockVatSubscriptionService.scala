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

package mocks

import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import connectors.httpParsers.UpdatePPOBHttpParser.UpdatePPOBResponse
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import services.VatSubscriptionService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockVatSubscriptionService extends MockitoSugar with BeforeAndAfterEach {
  this: Suite =>

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockVatSubscriptionService)
  }

  val mockVatSubscriptionService: VatSubscriptionService = mock[VatSubscriptionService]

  def mockUpdateEmailAddress(vrn: String, email: String)(response: Future[UpdatePPOBResponse]): Unit =
    when(mockVatSubscriptionService.updateEmail(
      ArgumentMatchers.eq(vrn),
      ArgumentMatchers.eq(email)
    )(ArgumentMatchers.any[HeaderCarrier],
      ArgumentMatchers.any[ExecutionContext])) thenReturn response

  def mockUpdateWebsite(vrn: String, website: String)(response: Future[UpdatePPOBResponse]): Unit =
    when(mockVatSubscriptionService.updateWebsite(
      ArgumentMatchers.eq(vrn),
      ArgumentMatchers.eq(website)
    )(ArgumentMatchers.any[HeaderCarrier],
      ArgumentMatchers.any[ExecutionContext])) thenReturn response

  def mockUpdatePhoneNumbers(vrn: String, landline: Option[String], mobile: Option[String])(response: Future[UpdatePPOBResponse]): Unit =
    when(mockVatSubscriptionService.updatePhoneNumbers(
      ArgumentMatchers.eq(vrn),
      ArgumentMatchers.eq(landline),
      ArgumentMatchers.eq(mobile)
    )(ArgumentMatchers.any[HeaderCarrier],
      ArgumentMatchers.any[ExecutionContext])) thenReturn response

  def mockGetCustomerInfo(vrn: String)(response: Future[GetCustomerInfoResponse]): Unit =
    when(mockVatSubscriptionService.getCustomerInfo(
      ArgumentMatchers.eq(vrn)
    )(ArgumentMatchers.any[HeaderCarrier],
      ArgumentMatchers.any[ExecutionContext])) thenReturn response
}
