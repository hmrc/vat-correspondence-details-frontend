/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.mobileNumber

import assets.BaseTestConstants._
import audit.models.ChangedMobileNumberAuditModel
import common.SessionKeys
import controllers.ControllerBaseSpec
import models.customerInformation.UpdatePPOBSuccess
import models.errors.ErrorModel
import org.jsoup.Jsoup
import org.mockito.Mockito.reset
import play.api.http.Status
import play.api.http.Status.{CONFLICT, INTERNAL_SERVER_ERROR}
import play.api.test.Helpers._
import views.html.templates.CheckYourAnswersView

import scala.concurrent.Future


class ConfirmMobileNumberControllerSpec extends ControllerBaseSpec  {

  val controller = new ConfirmMobileNumberController(
    mockErrorHandler,
    mockVatSubscriptionService,
    inject[CheckYourAnswersView],
    mockAuditingService
  )

  "Calling the show action in ConfirmMobileNumberController" when {

    "there is a mobile number in session" should {

      "show the Confirm mobile Number page" in {
        mockIndividualAuthorised()
        val result = controller.show(requestWithPrepopMobileNumber)

        status(result) shouldBe Status.OK
      }
    }

    "there isn't a mobile number in session" should {

      lazy val result = {
        mockIndividualAuthorised()
        controller.show(request)
      }

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to enter a new mobile number" in {
        redirectLocation(result) shouldBe Some(routes.CaptureMobileNumberController.show().url)

      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.show(requestWithPrepopMobileNumber)

        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }

  "Calling the updateMobileNumber() action in ConfirmMobileNumberController" when {

    "there is a mobile number in session" when {

      "the mobile number has been updated successfully" should {

        lazy val result = {
          mockIndividualAuthorised()
          mockUpdateMobileNumber(
            vrn, testPrepopMobile)(Future(Right(UpdatePPOBSuccess("success")))
          )
          controller.updateMobileNumber()(requestWithPrepopMobileNumber)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "audit the change mobile number event" in {
          verifyExtendedAudit(
            ChangedMobileNumberAuditModel(
              None,
              testPrepopMobile,
              vrn,
              isAgent = false,
              None
            )
          )
          reset(mockAuditingService)
        }

        "redirect to the success page" in {
          redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.mobileNumber().url)
        }

        "add the successful change key to the session" in {
          session(result).get(SessionKeys.mobileChangeSuccessful) shouldBe Some("true")
        }

        "add the inflight change key to the session" in {
          session(result).get(SessionKeys.inFlightContactDetailsChangeKey) shouldBe Some("true")
        }
      }

      "there was a conflict returned when trying to update the mobile number" should {

        lazy val result = {
          mockIndividualAuthorised()
          mockUpdateMobileNumber(vrn, testPrepopMobile)(
            Future(Left(ErrorModel(CONFLICT, "The back end has indicated there is an update already in progress"))))
          controller.updateMobileNumber()(requestWithPrepopMobileNumber)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect the user to the manage-vat overview page" in {
          redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
        }
      }

      "there was an unexpected error trying to update the mobile number" should {

        lazy val result = {
          mockIndividualAuthorised()
          mockUpdateMobileNumber(vrn, testPrepopMobile)(
            Future(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't verify mobile number"))))
          controller.updateMobileNumber()(requestWithPrepopMobileNumber)
        }

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "show the internal server error page" in {
          messages(Jsoup.parse(bodyOf(result)).title) shouldBe internalServerErrorTitle
        }
      }
    }

    "there isn't a mobile number in session" should {
      lazy val result = {
        mockIndividualAuthorised()
        controller.updateMobileNumber()(request)
      }

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to the capture mobile number page" in {
        redirectLocation(result) shouldBe Some(routes.CaptureMobileNumberController.show().url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.updateMobileNumber()(requestWithPrepopMobileNumber)

        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }
}
