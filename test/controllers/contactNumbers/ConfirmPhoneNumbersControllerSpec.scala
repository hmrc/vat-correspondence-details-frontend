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

import assets.BaseTestConstants._
import controllers.ControllerBaseSpec
import models.customerInformation.UpdatePPOBSuccess
import models.errors.ErrorModel
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.http.Status.{CONFLICT, INTERNAL_SERVER_ERROR}
import play.api.test.Helpers._
import views.html.contactNumbers.ConfirmPhoneNumbersView

import scala.concurrent.Future

class ConfirmPhoneNumbersControllerSpec extends ControllerBaseSpec  {

  val controller = new ConfirmPhoneNumbersController(
    mockAuthPredicateComponents,
    mcc,
    mockErrorHandler,
    mockVatSubscriptionService,
    injector.instanceOf[ConfirmPhoneNumbersView],
    mockConfig
  )

  "Calling the show action in ConfirmPhoneNumbersController" when {

    "there are phone numbers in session" should {

      "show the Confirm Phone Numbers page" in {
        mockIndividualAuthorised()
        val result = controller.show(requestWithPhoneNumbers)

        status(result) shouldBe Status.OK
      }
    }

    "there isn't a phone number in session" should {

      "take the user to enter a new phone number" in {
        mockIndividualAuthorised()
        val result = controller.show(request)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(controllers.contactNumbers.routes.ConfirmPhoneNumbersController.show().url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.show(requestWithPhoneNumbers)

        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }

  "Calling the updatePhoneNumbers() action in ConfirmPhoneNumbersController" when {

    "there is a phone number in session" when {
      "the phone numbers have been updated successfully" should {

        "show the phone numbers changed success page" in {
          mockIndividualAuthorised()

          mockUpdatePhoneNumbers(vrn, Some(testLandline), Some(testMobile))(Future(Right(UpdatePPOBSuccess("success"))))
          val result = controller.updatePhoneNumbers()(requestWithPhoneNumbers)

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.contactNumbers.routes.ConfirmPhoneNumbersController.show().url)
        }
      }

      "there was a conflict returned when trying to update the phone numbers" should {

        "redirect the user to the manage-vat page " in {
          mockIndividualAuthorised()
          mockUpdatePhoneNumbers(vrn, Some(testLandline), Some(testMobile))(
            Future(Left(ErrorModel(CONFLICT, "The back end has indicated there is an update already in progress"))))
          val result = controller.updatePhoneNumbers()(requestWithPhoneNumbers)

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
        }
      }

      "there was an unexpected error trying to update the phone numbers" should {

        "return an Internal Server Error" in {
          mockIndividualAuthorised()
          mockUpdatePhoneNumbers(vrn, Some(testLandline), Some(testMobile))(
            Future(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't verify phone numbers"))))
          val result = controller.updatePhoneNumbers()(requestWithPhoneNumbers)

          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
          messages(Jsoup.parse(bodyOf(result)).title) shouldBe internalServerErrorTitle
        }
      }
    }

    "there isn't a phone number in session" should {

      "take the user to the capture phone numbers page" in {
        mockIndividualAuthorised()
        val result = controller.updatePhoneNumbers()(request)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(controllers.contactNumbers.routes.ConfirmPhoneNumbersController.show().url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.updatePhoneNumbers()(requestWithPhoneNumbers)

        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }
}
