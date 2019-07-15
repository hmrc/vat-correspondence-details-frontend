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

package controllers

import assets.BaseTestConstants._
import common.SessionKeys
import models.User
import models.customerInformation.UpdatePPOBSuccess
import models.errors.ErrorModel
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.http.Status.{CONFLICT, INTERNAL_SERVER_ERROR}
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ConfirmRemoveWebsiteView

import scala.concurrent.Future

class ConfirmRemoveWebsiteControllerSpec extends ControllerBaseSpec  {

  object TestConfirmRemoveWebsiteController extends ConfirmRemoveWebsiteController(
    mockAuthPredicateComponents,
    mockInflightPPOBPredicate,
    mcc,
    mockErrorHandler,
    mockVatSubscriptionService,
    injector.instanceOf[ConfirmRemoveWebsiteView],
    mockConfig
  )
  lazy val requestWithValidationWebsiteKey: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(SessionKeys.validationWebsiteKey-> testWebsite)

  "Calling the extractWebsite function in ConfirmWebsiteController" when {

    "there is an authenticated request from a user with an website address in session" should {

      "result in an website address being retrieved if there is an website" in {
        val user = User[AnyContent](vrn, active = true, None)(requestWithValidationWebsiteKey)

        TestConfirmRemoveWebsiteController.extractSessionWebsiteAddress(user) shouldBe Some(testWebsite)
      }
    }
  }

  "Calling the show action in ConfirmWebsiteController" when {

    "there is an website address in session" should {

      "show the Confirm website page" in {
        val result = TestConfirmRemoveWebsiteController.show(requestWithValidationWebsiteKey)

        status(result) shouldBe Status.OK
      }
    }

    "there isn't an website address in session" should {

      "take the user to enter a new website address" in {
        val result = TestConfirmRemoveWebsiteController.show(request)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(controllers.routes.CaptureWebsiteController.show().url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = TestConfirmRemoveWebsiteController.show(requestWithValidationWebsiteKey)

        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }

  "Calling the removeWebsiteAddress() action in ConfirmWebsiteController" when {

    "there is a verified website address in session" when {

      "the website has been updated successfully" should {

        "show the website address changed success page" in {
          mockUpdateWebsite(vrn, "")(Future(Right(UpdatePPOBSuccess("success"))))
          val result = TestConfirmRemoveWebsiteController.removeWebsiteAddress()(requestWithValidationWebsiteKey)
//TODO update the test to redirect it to the correct address
//          status(result) shouldBe Status.SEE_OTHER
//          redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/remove-website-address")
        }
      }

      "there was a conflict returned when trying to update the website address" should {

        "redirect the user to the manage-vat page " in {
          mockUpdateWebsite(vrn, "")(
            Future(Left(ErrorModel(CONFLICT, "The back end has indicated there is an update already in progress"))))
          val result = TestConfirmRemoveWebsiteController.removeWebsiteAddress()(requestWithValidationWebsiteKey)

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
        }
      }

      "there was an unexpected error trying to update the website address" should {

        "return an Internal Server Error" in {
          mockUpdateWebsite(vrn, "")(
            Future(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't verify website address"))))
          val result = TestConfirmRemoveWebsiteController.removeWebsiteAddress()(requestWithValidationWebsiteKey)

          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
          messages(Jsoup.parse(bodyOf(result)).title) shouldBe internalServerErrorTitle
        }
      }
    }

    "there isn't an website address in session" should {

      "take the user to the capture website page" in {
        val result = TestConfirmRemoveWebsiteController.removeWebsiteAddress()(request)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(controllers.routes.CaptureWebsiteController.show().url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = TestConfirmRemoveWebsiteController.removeWebsiteAddress()(requestWithValidationWebsiteKey)

        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }
}
