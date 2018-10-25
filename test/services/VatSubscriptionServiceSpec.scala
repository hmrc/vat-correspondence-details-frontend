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

import assets.CustomerInfoConstants._
import mocks.{MockEmailVerificationService, MockVatSubscriptionConnector}
import models.errors.{EmailAddressUpdateResponseModel, ErrorModel}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import utils.TestUtil

import scala.concurrent.Future

class VatSubscriptionServiceSpec extends TestUtil with MockVatSubscriptionConnector with MockEmailVerificationService {

  val service = new VatSubscriptionService(connector, mockEmailVerificationService)

  "The getCustomerInfo function" when {

    "a CustomerInformation model is returned by the connector" should {

      "return the model" in {
        mockGetCustomerInfoSuccessResponse()
        val result = await(service.getCustomerInfo("123456789"))
        result shouldBe Right(fullCustomerInfoModel)
      }
    }

    "an error is returned by the connector" should {

      "return the error" in {
        mockGetCustomerInfoFailureResponse()
        val result = await(service.getCustomerInfo("123456789"))
        result shouldBe Left(invalidJsonError)
      }
    }
  }

  "The updateEmailAddress function" when {

    "the email has been verified and the email update has been successful" should {

      "return an email address update response model" in {
        mockGetEmailVerificationState("test@email.com")(Future(Some(true)))
        mockUpdateEmailAddressSuccessResponse()
        val result = await(service.updateEmailAddress("test@email.com", "123456789"))
        result shouldBe Right(EmailAddressUpdateResponseModel(true))
      }
    }

    "the email has been verified but the email update was not successful" should {

      "return an error model" in {
        mockGetEmailVerificationState("test@email.com")(Future(Some(true)))
        mockUpdateEmailAddressFailureResponse()
        val result = await(service.updateEmailAddress("test@email.com", "123456789"))
        result shouldBe Left(ErrorModel(NOT_FOUND, "Couldn't find a user with VRN provided"))
      }
    }

    "the email has not been verified" should {

      "return an error model" in {
        mockGetEmailVerificationState("test@email.com")(Future(Some(false)))
        val result = await(service.updateEmailAddress("test@email.com", "123456789"))
        result shouldBe Right(EmailAddressUpdateResponseModel(false))
      }
    }

    "there was an unexpected response from email verification service" should {

      "return an error model" in {
        mockGetEmailVerificationState("test@email.com")(Future(None))
        val result = await(service.updateEmailAddress("test@email.com", "123456789"))
        result shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't verify email address"))
      }
    }
  }
}
