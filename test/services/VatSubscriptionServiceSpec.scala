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

import play.api.http.Status
import assets.CustomerInfoConstants._
import mocks.{MockEmailVerificationService, MockVatSubscriptionConnector}
import models.customerInformation._
import models.errors.ErrorModel
import utils.TestUtil

import scala.concurrent.Future

class VatSubscriptionServiceSpec extends TestUtil with MockVatSubscriptionConnector with MockEmailVerificationService {

  val service = new VatSubscriptionService(connector, mockEmailVerificationService)

  val testVrn: String   = "123456789"
  val testEmail: String = "test@example.com"

  "calling getCustomerInfo" when {

    "the VatSubscriptionConnector returns a model" should {

      "return the model" in {
        mockGetCustomerInfoSuccessResponse()

        val result = await(service.getCustomerInfo(testVrn))
        result shouldBe Right(fullCustomerInfoModel)
      }
    }

    "the VatSubscriptionConnector returns an error" should {

      "return the error" in {
        mockGetCustomerInfoFailureResponse()

        val result = await(service.getCustomerInfo(testVrn))
        result shouldBe Left(invalidJsonError)
      }
    }
  }

  "calling updateEmail" when {

    "the email has been verified and the email update has been successful" should {

      "return the model" in {
        mockGetEmailVerificationState(testEmail)(Future(Some(true)))
        mockGetCustomerInfoSuccessResponse()
        mockUpdateEmailSuccessResponse()

        val result = await(service.updateEmail(testVrn, testEmail))
        result shouldBe Right(UpdateEmailSuccess("success"))
      }
    }

    "the VatSubscriptionConnector returns an error" should {

      "return the error" in {
        mockGetEmailVerificationState(testEmail)(Future(Some(true)))
        mockGetCustomerInfoSuccessResponse()
        mockUpdateEmailFailureResponse()

        val result = await(service.updateEmail(testVrn, testEmail))
        result shouldBe Left(invalidJsonError)
      }
    }

    "the email has not been verified" should {

      "return the error" in {
        mockGetEmailVerificationState(testEmail)(Future(Some(false)))

        val result = await(service.updateEmail(testVrn, testEmail))
        result shouldBe Right(UpdateEmailSuccess(""))
      }
    }

    "there was an unexpected response from email verification service" should {

      "return the error" in {
        mockGetEmailVerificationState(testEmail)(Future(None))

        val result = await(service.updateEmail(testVrn, testEmail))
        result shouldBe Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "Couldn't verify email address"))
      }
    }
  }

  "calling buildEmailUpdateModel" should {

    "return a CustomerInformation model with the updated email" in {
      val expectedPPOB: PPOB = PPOB(
        fullPPOBAddressModel,
        Some(ContactDetails(
          Some("01234567890"),
          Some("07707707707"),
          Some("0123456789"),
          Some(testEmail),
          Some(true)
        )),
        Some("www.pepsi-mac.biz")
      )

      val result = service.buildEmailUpdateModel(testEmail, fullPPOBModel)
      result shouldBe expectedPPOB
    }
  }
}
