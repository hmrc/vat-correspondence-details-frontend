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

package controllers.contactPreference

import assets.BaseTestConstants.vrn
import assets.CustomerInfoConstants._
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import controllers.ControllerBaseSpec
import mocks.MockVatSubscriptionService
import models.customerInformation.CustomerInformation
import models.errors.ErrorModel
import play.api.test.Helpers._

import scala.concurrent.Future

class ContactPreferenceRedirectControllerSpec extends ControllerBaseSpec with MockVatSubscriptionService {

  val controller = new ContactPreferenceRedirectController(mockErrorHandler)(
    mockConfig,
    mcc,
    mockAuthPredicateComponents,
    mockInFlightPredicateComponents,
    mockVatSubscriptionService
  )

  implicit def convertToFutureRight: CustomerInformation => Future[GetCustomerInfoResponse] = customerDetails =>
    Future.successful(Right(customerDetails))

  implicit def convertToFutureLeft: ErrorModel => Future[GetCustomerInfoResponse] = error =>
    Future.successful(Left(error))

  "Calling .redirect as a user" should {

    "return a redirect" when {

      "the user has a paper preference" which {
        val result = {
          mockGetCustomerInfo(vrn)(fullCustomerInfoModel.copy(commsPreference = Some("PAPER")))
          mockIndividualAuthorised()
          controller.redirect()(fakeRequestWithClientsVRN)
        }

        s"returns a $SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }

        "has the redirect url for the change email journey" in {
          redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/contact-preference-email")
        }

      }

      "the user has an email preference" which {

        val result = {
          mockGetCustomerInfo(vrn)(fullCustomerInfoModel)
          mockIndividualAuthorised()
          controller.redirect()(fakeRequestWithClientsVRN)
        }

        s"returns a $SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }

        "has the redirect url for the change address journey" in {
          redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/contact-preference-letter")
        }

      }

    }

    "return an internal server error" when {

      "the customer details are not returned" in {
        val result = {
          mockGetCustomerInfo(vrn)(ErrorModel(BAD_REQUEST, "nu-uh"))
          mockIndividualAuthorised()
          controller.redirect()(fakeRequestWithClientsVRN)
        }

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "the user has no comms preference" in {
        val result = {
          mockGetCustomerInfo(vrn)(fullCustomerInfoModel.copy(commsPreference = None))
          mockIndividualAuthorised()
          controller.redirect()(fakeRequestWithClientsVRN)
        }

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

    }

  }

  "Calling .redirect as an agent" should {

    s"return a $UNAUTHORIZED response" in {
      val result = {
        mockAgentAuthorised()
        controller.redirect()(request)
      }
      status(result) shouldBe UNAUTHORIZED

    }

  }

}
