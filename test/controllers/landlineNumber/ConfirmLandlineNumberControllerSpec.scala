/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.http.Status
import play.api.http.Status.{CONFLICT, INTERNAL_SERVER_ERROR}
import play.api.test.Helpers._
import views.html.landlineNumber.ConfirmRemoveLandlineView
import views.html.templates.CheckYourAnswersView

import scala.concurrent.Future

class ConfirmLandlineNumberControllerSpec extends ControllerBaseSpec  {

  val controller = new ConfirmLandlineNumberController(
    mockErrorHandler,
    mockVatSubscriptionService,
    inject[CheckYourAnswersView],
    inject[ConfirmRemoveLandlineView],
    mockAuditingService
  )

  "Calling the show action in ConfirmLandlineNumberController" when {

    "there is a non-empty landline number in session" when {

      "there is a non-empty old landline number in session" should {

        "show the Confirm landline Number page" in {
          mockIndividualAuthorised
          val result = controller.show(getRequestWithAllLandlineNumbers)

          status(result) shouldBe Status.OK
        }
      }

      "there is an empty old landline number in session" should {

        "show the Confirm landline Number page" in {
          mockIndividualAuthorised
          val result = controller.show(getRequestWithPrepopLandlineNumber.withSession(SessionKeys.validationLandlineKey -> ""))

          status(result) shouldBe Status.OK
        }
      }
    }

    "there is an empty landline number in session" when {

      "there is a non-empty old landline number in session" should {

        "redirect to the capture landline number page" in {
          mockIndividualAuthorised
          val result = controller.show(getRequest.withSession(SessionKeys.validationLandlineKey -> testValidationLandline,
            SessionKeys.prepopulationLandlineKey -> ""))

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CaptureLandlineNumberController.show.url)
        }
      }

      "there is an empty old landline number in session" should {

        "redirect to the capture landline number page" in {
          mockIndividualAuthorised
          val result = controller.show(getRequest.withSession(SessionKeys.validationLandlineKey -> "", SessionKeys.prepopulationLandlineKey -> ""))

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CaptureLandlineNumberController.show.url)
        }
      }
    }

    "there isn't a landline number in session" should {

      "there is an old landline number in session" should {

        lazy val result = {
          mockIndividualAuthorised
          controller.show(getRequestWithValidationLandlineNumber)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect the user to enter a new landline number" in {
          redirectLocation(result) shouldBe Some(routes.CaptureLandlineNumberController.show.url)
        }
      }

      "there is no old landline number in session" should {

        lazy val result = {
          mockIndividualAuthorised
          controller.show(getRequest)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect the user to enter a new landline number" in {
          redirectLocation(result) shouldBe Some(routes.CaptureLandlineNumberController.show.url)
        }
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment
        val result = controller.show(getRequestWithPrepopLandlineNumber)

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    insolvencyCheck(controller.show())
  }

  "Calling the updateLandlineNumber() action in ConfirmLandlineNumberController" when {

    "there is a landline number in session" when {

      "there is a non-empty validation landline number in session" when {

        "the landline number has been updated successfully" should {

          lazy val result = {
            mockIndividualAuthorised
            mockUpdateLandlineNumber(vrn, testPrepopLandline)(Future(Right(UpdatePPOBSuccess("success"))))
            controller.updateLandlineNumber()(postRequestWithAllLandlineNumbers)
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "audit the change landline number event" in {
            verifyExtendedAudit(
              ChangedLandlineNumberAuditModel(Some(testValidationLandline), testPrepopLandline, vrn, None)
            )
          }

          "redirect to the success page" in {
            redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.landlineNumber.url)
          }

          "add the successful change key to the session" in {
            session(result).get(SessionKeys.landlineChangeSuccessful) shouldBe Some("true")
          }

          "add the inflight change key to the session" in {
            session(result).get(SessionKeys.inFlightContactDetailsChangeKey) shouldBe Some("true")
          }

          "remove the existing landline number from the session" in {
            session(result).get(SessionKeys.validationLandlineKey) shouldBe None
          }

          "remove the landline prepop value from the session" in {
            session(result).get(SessionKeys.prepopulationLandlineKey) shouldBe None
          }
        }
      }

      "there is an empty validation landline number in session" when {

        "the landline number has been updated successfully" should {

          lazy val result = {
            mockIndividualAuthorised
            mockUpdateLandlineNumber(vrn, testPrepopLandline)(Future(Right(UpdatePPOBSuccess("success"))))
            controller.updateLandlineNumber()(postRequestWithPrepopLandlineNumber.withSession(SessionKeys.validationLandlineKey -> ""))
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "audit the change landline number event" in {
            verifyExtendedAudit(ChangedLandlineNumberAuditModel(None, testPrepopLandline, vrn, None))
          }

          "redirect to the success page" in {
            redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.landlineNumber.url)
          }

          "add the successful change key to the session" in {
            session(result).get(SessionKeys.landlineChangeSuccessful) shouldBe Some("true")
          }

          "add the inflight change key to the session" in {
            session(result).get(SessionKeys.inFlightContactDetailsChangeKey) shouldBe Some("true")
          }

          "remove the existing landline number from the session" in {
            session(result).get(SessionKeys.validationLandlineKey) shouldBe None
          }

          "remove the landline prepop value from the session" in {
            session(result).get(SessionKeys.prepopulationLandlineKey) shouldBe None
          }
        }
      }

      "there is no validation landline number in session" when {

        "the landline number has been updated successfully" should {

          lazy val result = {
            mockIndividualAuthorised
            mockUpdateLandlineNumber(vrn, testPrepopLandline)(Future(Right(UpdatePPOBSuccess("success"))))
            controller.updateLandlineNumber()(postRequestWithPrepopLandlineNumber)
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "audit the change landline number event" in {
            verifyExtendedAudit(ChangedLandlineNumberAuditModel(None, testPrepopLandline, vrn, None))
          }

          "redirect to the success page" in {
            redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.landlineNumber.url)
          }

          "add the successful change key to the session" in {
            session(result).get(SessionKeys.landlineChangeSuccessful) shouldBe Some("true")
          }

          "add the inflight change key to the session" in {
            session(result).get(SessionKeys.inFlightContactDetailsChangeKey) shouldBe Some("true")
          }

          "remove the landline prepop value from the session" in {
            session(result).get(SessionKeys.prepopulationLandlineKey) shouldBe None
          }
        }
      }

      "there was a conflict returned when trying to update the landline number" should {

        lazy val result = {
          mockIndividualAuthorised
          mockUpdateLandlineNumber(vrn, testPrepopLandline)(
            Future(Left(ErrorModel(CONFLICT, "The back end has indicated there is an update already in progress"))))
          controller.updateLandlineNumber()(postRequestWithPrepopLandlineNumber)
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
          mockIndividualAuthorised
          mockUpdateLandlineNumber(vrn, testPrepopLandline)(
            Future(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't verify landline number"))))
          controller.updateLandlineNumber()(postRequestWithPrepopLandlineNumber)
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
        mockIndividualAuthorised
        controller.updateLandlineNumber()(getRequest)
      }

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to the capture landline number page" in {
        redirectLocation(result) shouldBe Some(routes.CaptureLandlineNumberController.show.url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment
        val result = controller.updateLandlineNumber()(postRequestWithPrepopLandlineNumber)

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    insolvencyCheck(controller.updateLandlineNumber())
  }

  "Calling the removeShow() action" when {

    "there is a validation landline number in session" should {

      "return 200" in {
        val result = controller.removeShow()(postRequestWithValidationLandlineNumber)
        status(result) shouldBe Status.OK
      }
    }

    "there isn't a validation landline number in session" should {

      lazy val result = controller.removeShow()(getRequest)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the capture landline page" in {
        redirectLocation(result) shouldBe Some(routes.CaptureLandlineNumberController.show.url)
      }
    }

    "the user is not authorised" should {

      "return 403" in {
        val result = {
          mockIndividualWithoutEnrolment
          controller.removeShow()(postRequest)
        }

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    insolvencyCheck(controller.removeShow())
  }

  "Calling the removeLandlineNumber() action" when {

    "there is a validation landline number in session" when {

      "the form has errors" should {

        lazy val result = controller.removeLandlineNumber()(postRequestWithValidationLandlineNumber)

        "return 400" in {
          status(result) shouldBe Status.BAD_REQUEST
        }
      }

      "the form is submitted successfully" when {

        "a Yes is submitted" should {

          lazy val result = {
            mockUpdateLandlineNumber(vrn, "")(Future(Right(UpdatePPOBSuccess("success"))))
            controller.removeLandlineNumber()(
              postRequestWithValidationLandlineNumber.withFormUrlEncodedBody("yes_no" -> "yes"))
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the landlineNumber action in ChangeSuccessController" in {
            redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.landlineNumber.url)
          }

          "audit the change landline number event" in {
            verifyExtendedAudit(ChangedLandlineNumberAuditModel(Some(testValidationLandline), "", vrn, None))
          }

          "remove the existing landline number from the session" in {
            session(result).get(SessionKeys.validationLandlineKey) shouldBe None
          }

          "add the successful change key to the session" in {
            session(result).get(SessionKeys.landlineChangeSuccessful) shouldBe Some("true")
          }

          "add the inflight change key to the session" in {
            session(result).get(SessionKeys.inFlightContactDetailsChangeKey) shouldBe Some("true")
          }
        }

        "a No is submitted" should {

          lazy val result = controller.removeLandlineNumber()(
            postRequestWithValidationLandlineNumber.withFormUrlEncodedBody("yes_no" -> "no"))

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the business details page" in {
            redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
          }
        }
      }
    }

    "there isn't a validation landline number in session" should {

      lazy val result = controller.removeLandlineNumber()(postRequest)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the capture landline page" in {
        redirectLocation(result) shouldBe Some(routes.CaptureLandlineNumberController.show.url)
      }
    }

    "the user is not authorised" should {

      "return 403" in {
        val result = {
          mockIndividualWithoutEnrolment
          controller.removeLandlineNumber()(postRequest)
        }

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    insolvencyCheck(controller.removeLandlineNumber())
  }
}
