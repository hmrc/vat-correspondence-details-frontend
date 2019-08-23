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
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import services.VatSubscriptionService

import scala.concurrent.Future

trait MockVatSubscriptionService extends MockitoSugar with BeforeAndAfterEach {
  this: Suite =>

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockVatSubscriptionService)
  }

  val mockVatSubscriptionService: VatSubscriptionService = mock[VatSubscriptionService]

  def mockUpdateEmailAddress(vrn: String, email: String)(response: Future[UpdatePPOBResponse]): Unit =
    when(mockVatSubscriptionService.updateEmail(argEq(vrn), argEq(email))(any(), any(), any())) thenReturn response

  def mockUpdateWebsite(vrn: String, website: String)(response: Future[UpdatePPOBResponse]): Unit =
    when(mockVatSubscriptionService.updateWebsite(argEq(vrn), argEq(website))(any(), any(), any())) thenReturn response

  def mockUpdatePhoneNumbers(vrn: String, landline: Option[String], mobile: Option[String])
                            (response: Future[UpdatePPOBResponse]): Unit =
    when(mockVatSubscriptionService.updateContactNumbers(
      argEq(vrn), argEq(landline), argEq(mobile))(any(), any(), any())
    ) thenReturn response

  def mockGetCustomerInfo(vrn: String)(response: Future[GetCustomerInfoResponse]): Unit =
    when(mockVatSubscriptionService.getCustomerInfo(argEq(vrn))(any(), any())) thenReturn response
}
