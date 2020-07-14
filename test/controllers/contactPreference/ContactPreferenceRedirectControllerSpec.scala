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
import controllers.ControllerBaseSpec
import mocks.MockVatSubscriptionService
import models.errors.ErrorModel
import play.api.test.Helpers._

class ContactPreferenceRedirectControllerSpec extends ControllerBaseSpec with MockVatSubscriptionService {

  val controller = new ContactPreferenceRedirectController(mockErrorHandler)(
    mockConfig,
    mcc,
    mockAuthPredicateComponents,
    mockInFlightPredicateComponents,
    mockVatSubscriptionService
  )

  "Calling .redirect as a user" should {

    "return a redirect" when {

      "the user has a paper preference" which {

        lazy val result = {
          mockGetCustomerInfo(vrn)(Right(fullCustomerInfoModel.copy(commsPreference = Some("PAPER"))))
          controller.redirect(request)
        }

        s"returns a $SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }

        "has the redirect url for the change email journey" in {
          redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/contact-preference-email")
        }

      }

      "the user has an email preference" which {

        lazy val result = {
          mockGetCustomerInfo(vrn)(Right(fullCustomerInfoModel))
          controller.redirect(request)
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
          mockGetCustomerInfo(vrn)(Left(ErrorModel(BAD_REQUEST, "nu-uh")))
          controller.redirect(request)
        }

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "the user has no comms preference" in {
        val result = {
          mockGetCustomerInfo(vrn)(Right(fullCustomerInfoModel.copy(commsPreference = None)))
          controller.redirect(request)
        }

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

    }

  }

  "Calling .redirect as an agent" should {

    "redirect them to the agent hub page" in {
      val result = {
        mockAgentAuthorised()
        controller.redirect(fakeRequestWithClientsVRN)
      }
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(mockConfig.vatAgentClientLookupAgentHubPath)
    }

  }

}
