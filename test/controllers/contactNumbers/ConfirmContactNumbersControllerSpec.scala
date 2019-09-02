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

package controllers.contactNumbers

import controllers.ControllerBaseSpec
import models.customerInformation.UpdatePPOBSuccess
import models.errors.ErrorModel
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.http.Status.{CONFLICT, INTERNAL_SERVER_ERROR}
import play.api.test.Helpers._
import views.html.contactNumbers.ConfirmContactNumbersView

import scala.concurrent.Future
import assets.BaseTestConstants._
import common.SessionKeys

class ConfirmContactNumbersControllerSpec extends ControllerBaseSpec  {

  val controller = new ConfirmContactNumbersController(
    mockErrorHandler,
    mockVatSubscriptionService,
    injector.instanceOf[ConfirmContactNumbersView]
  )

  "Calling the show action in ConfirmContactNumbersController" when {

    "there are contact numbers in session" should {

      "show the Confirm Contact Numbers page" in {
        mockIndividualAuthorised()
        val result = controller.show(requestWithPrepopPhoneNumbers)

        status(result) shouldBe Status.OK
      }
    }

    "there isn't a contact number in session" should {

      "take the user to enter a new contact number" in {
        mockIndividualAuthorised()
        val result = controller.show(request)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(controllers.contactNumbers.routes.CaptureContactNumbersController.show().url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.show(requestWithPrepopPhoneNumbers)

        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }

  "Calling the updateContactNumbers() action in ConfirmContactNumbersController" when {

    "there is a contact number in session" when {

      "the contact numbers have been updated successfully" should {

        lazy val result = {
          mockIndividualAuthorised()
          mockUpdatePhoneNumbers(
            vrn, Some(testPrepopLandline), Some(testPrepopMobile))(Future(Right(UpdatePPOBSuccess("success")))
          )
          controller.updateContactNumbers()(requestWithPrepopPhoneNumbers)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to the success page" in {
          redirectLocation(result) shouldBe Some(routes.ContactNumbersChangeSuccessController.show().url)
        }

        "add the successful change key to the session" in {
          session(result).get(SessionKeys.phoneNumberChangeSuccessful) shouldBe Some("true")
        }

        "add the inflight change key to the session" in {
          session(result).get(SessionKeys.inFlightContactDetailsChangeKey) shouldBe Some("telephone")
        }
      }

      "there was a conflict returned when trying to update the contact numbers" should {

        "redirect the user to the manage-vat page " in {
          mockIndividualAuthorised()
          mockUpdatePhoneNumbers(vrn, Some(testPrepopLandline), Some(testPrepopMobile))(
            Future(Left(ErrorModel(CONFLICT, "The back end has indicated there is an update already in progress"))))
          val result = controller.updateContactNumbers()(requestWithPrepopPhoneNumbers)

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
        }
      }

      "there was an unexpected error trying to update the contact numbers" should {

        "return an Internal Server Error" in {
          mockIndividualAuthorised()
          mockUpdatePhoneNumbers(vrn, Some(testPrepopLandline), Some(testPrepopMobile))(
            Future(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't verify phone numbers"))))
          val result = controller.updateContactNumbers()(requestWithPrepopPhoneNumbers)

          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
          messages(Jsoup.parse(bodyOf(result)).title) shouldBe internalServerErrorTitle
        }
      }
    }

    "there isn't a contact number in session" should {

      "take the user to the capture contact numbers page" in {
        mockIndividualAuthorised()
        val result = controller.updateContactNumbers()(request)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(controllers.contactNumbers.routes.ConfirmContactNumbersController.show().url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.updateContactNumbers()(requestWithPrepopPhoneNumbers)

        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }
}
