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

package controllers.landlineNumber

import assets.BaseTestConstants._
import audit.models.ChangedLandlineNumberAuditModel
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


class ConfirmLandlineNumberControllerSpec extends ControllerBaseSpec  {

  val controller = new ConfirmLandlineNumberController(
    mockErrorHandler,
    mockVatSubscriptionService,
    inject[CheckYourAnswersView],
    mockAuditingService
  )

  "Calling the show action in ConfirmLandlineNumberController" when {

    "there is a landline number in session" should {

      "show the Confirm landline Number page" in {
        mockIndividualAuthorised()
        val result = controller.show(requestWithPrepopLandlineNumber)

        status(result) shouldBe Status.OK
      }
    }

    "there isn't a landline number in session" should {

      lazy val result = {
        mockIndividualAuthorised()
        controller.show(request)
      }

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to enter a new landline number" in {
        redirectLocation(result) shouldBe Some(routes.CaptureLandlineNumberController.show().url)

      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.show(requestWithPrepopLandlineNumber)

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    insolvencyCheck(controller.show())
  }

  "Calling the updateLandlineNumber() action in ConfirmLandlineNumberController" when {

    "there is a landline number in session" when {

      "the landline number has been updated successfully" should {

        lazy val result = {
          mockIndividualAuthorised()
          mockUpdateLandlineNumber(
            vrn, testPrepopLandline)(Future(Right(UpdatePPOBSuccess("success")))
          )
          controller.updateLandlineNumber()(requestWithPrepopLandlineNumber)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "audit the change landline number event" in {
          verifyExtendedAudit(
            ChangedLandlineNumberAuditModel(
              None,
              testPrepopLandline,
              vrn,
              isAgent = false,
              None
            )
          )
          reset(mockAuditingService)
        }

        "redirect to the success page" in {
          redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.landlineNumber().url)
        }

        "add the successful change key to the session" in {
          session(result).get(SessionKeys.landlineChangeSuccessful) shouldBe Some("true")
        }

        "add the inflight change key to the session" in {
          session(result).get(SessionKeys.inFlightContactDetailsChangeKey) shouldBe Some("true")
        }
      }

      "there was a conflict returned when trying to update the landline number" should {

        lazy val result = {
          mockIndividualAuthorised()
          mockUpdateLandlineNumber(vrn, testPrepopLandline)(
            Future(Left(ErrorModel(CONFLICT, "The back end has indicated there is an update already in progress"))))
          controller.updateLandlineNumber()(requestWithPrepopLandlineNumber)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect the user to the manage-vat overview page" in {
          redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
        }
      }

      "there was an unexpected error trying to update the landline number" should {

        lazy val result = {
          mockIndividualAuthorised()
          mockUpdateLandlineNumber(vrn, testPrepopLandline)(
            Future(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't verify landline number"))))
          controller.updateLandlineNumber()(requestWithPrepopLandlineNumber)
        }

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "show the internal server error page" in {
          messages(Jsoup.parse(contentAsString(result)).title) shouldBe internalServerErrorTitle
        }
      }
    }

    "there isn't a landline number in session" should {
      lazy val result = {
        mockIndividualAuthorised()
        controller.updateLandlineNumber()(request)
      }

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to the capture landline number page" in {
        redirectLocation(result) shouldBe Some(routes.CaptureLandlineNumberController.show().url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.updateLandlineNumber()(requestWithPrepopLandlineNumber)

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    insolvencyCheck(controller.updateLandlineNumber())
  }
}
