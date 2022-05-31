/*
 * Copyright 2022 HM Revenue & Customs
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
import views.html.mobileNumber.ConfirmRemoveMobileView
import views.html.templates.CheckYourAnswersView

import scala.concurrent.Future


class ConfirmMobileNumberControllerSpec extends ControllerBaseSpec  {

  val controller = new ConfirmMobileNumberController(
    mockErrorHandler,
    mockVatSubscriptionService,
    inject[CheckYourAnswersView],
    inject[ConfirmRemoveMobileView],
    mockAuditingService
  )

  "Calling the show action in ConfirmMobileNumberController" when {

    "there is a non-empty mobile number in session" when {

      "there is a non-empty old mobile number in session" should {

        "show the Confirm mobile number page" in {
          mockIndividualAuthorised()
          val result = controller.show(getRequestWithAllMobileNumbers)

          status(result) shouldBe Status.OK
        }
      }

      "there is an empty old landline number in session" should {

        "show the Confirm mobile number page" in {
          mockIndividualAuthorised()
          val result = controller.show(getRequestWithPrepopMobileNumber.withSession(SessionKeys.validationMobileKey -> ""))

          status(result) shouldBe Status.OK
        }
      }
    }

    "there is an empty mobile number in session" when {

      "there is a non-empty old mobile number in session" should {

        "redirect to the capture mobile number page" in {
          mockIndividualAuthorised()
          val result = controller.show(getRequest.withSession(SessionKeys.validationMobileKey -> testValidationMobile,
            SessionKeys.prepopulationMobileKey -> ""))

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CaptureMobileNumberController.show.url)
        }
      }

      "there is an empty old mobile number in session" should {

        "redirect to the capture mobile number page" in {
          mockIndividualAuthorised()
          val result = controller.show(getRequest.withSession(SessionKeys.validationMobileKey -> "", SessionKeys.prepopulationMobileKey -> ""))

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CaptureMobileNumberController.show.url)
        }
      }
    }

    "there isn't a mobile number in session" should {

      "there is an old mobile number in session" should {

        lazy val result = {
          mockIndividualAuthorised()
          controller.show(getRequestWithValidationMobileNumber)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect the user to enter a new mobile number" in {
          redirectLocation(result) shouldBe Some(routes.CaptureMobileNumberController.show.url)
        }
      }

      "there is no old landline number in session" should {

        lazy val result = {
          mockIndividualAuthorised()
          controller.show(getRequest)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect the user to enter a new landline number" in {
          redirectLocation(result) shouldBe Some(routes.CaptureMobileNumberController.show.url)
        }
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.show(getRequestWithPrepopMobileNumber)

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    insolvencyCheck(controller.show)
  }

  "Calling the updateMobileNumber() action in ConfirmMobileNumberController" when {

    "there is a mobile number in session" when {

      "there is a non-empty validation mobile number in session" when {

        "the mobile number has been updated successfully" should {

          lazy val result = {
            mockIndividualAuthorised()
            mockUpdateMobileNumber(vrn, testPrepopMobile)(Future(Right(UpdatePPOBSuccess("success"))))
            controller.updateMobileNumber()(postRequestWithAllMobileNumbers)
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "audit the change mobile number event" in {
            verifyExtendedAudit(
              ChangedMobileNumberAuditModel(Some(testValidationMobile), testPrepopMobile, vrn, isAgent = false, None)
            )
            reset(mockAuditingService)
          }

          "redirect to the success page" in {
            redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.mobileNumber.url)
          }

          "add the successful change key to the session" in {
            session(result).get(SessionKeys.mobileChangeSuccessful) shouldBe Some("true")
          }

          "add the inflight change key to the session" in {
            session(result).get(SessionKeys.inFlightContactDetailsChangeKey) shouldBe Some("true")
          }

          "remove the existing mobile number from the session" in {
            session(result).get(SessionKeys.validationMobileKey) shouldBe None
          }

          "remove the mobile prepop value from the session" in {
            session(result).get(SessionKeys.prepopulationMobileKey) shouldBe None
          }
        }
      }

      "there is an empty validation mobile number in session" when {

        "the mobile number has been updated successfully" should {

          lazy val result = {
            mockIndividualAuthorised()
            mockUpdateMobileNumber(vrn, testPrepopMobile)(Future(Right(UpdatePPOBSuccess("success"))))
            controller.updateMobileNumber()(postRequestWithPrepopMobileNumber.withSession(SessionKeys.validationMobileKey -> ""))
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "audit the change mobile number event" in {
            verifyExtendedAudit(ChangedMobileNumberAuditModel(None, testPrepopMobile, vrn, isAgent = false, None))
            reset(mockAuditingService)
          }

          "redirect to the success page" in {
            redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.mobileNumber.url)
          }

          "add the successful change key to the session" in {
            session(result).get(SessionKeys.mobileChangeSuccessful) shouldBe Some("true")
          }

          "add the inflight change key to the session" in {
            session(result).get(SessionKeys.inFlightContactDetailsChangeKey) shouldBe Some("true")
          }

          "remove the existing mobile number from the session" in {
            session(result).get(SessionKeys.validationMobileKey) shouldBe None
          }

          "remove the mobile prepop value from the session" in {
            session(result).get(SessionKeys.prepopulationMobileKey) shouldBe None
          }
        }
      }

      "there is no validation mobile number in session" when {

        "the mobile number has been updated successfully" should {

          lazy val result = {
            mockIndividualAuthorised()
            mockUpdateMobileNumber(vrn, testPrepopMobile)(Future(Right(UpdatePPOBSuccess("success"))))
            controller.updateMobileNumber()(postRequestWithPrepopMobileNumber)
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "audit the change mobile number event" in {
            verifyExtendedAudit(ChangedMobileNumberAuditModel(None, testPrepopMobile, vrn, isAgent = false, None))
            reset(mockAuditingService)
          }

          "redirect to the success page" in {
            redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.mobileNumber.url)
          }

          "add the successful change key to the session" in {
            session(result).get(SessionKeys.mobileChangeSuccessful) shouldBe Some("true")
          }

          "add the inflight change key to the session" in {
            session(result).get(SessionKeys.inFlightContactDetailsChangeKey) shouldBe Some("true")
          }

          "remove the mobile prepop value from the session" in {
            session(result).get(SessionKeys.prepopulationMobileKey) shouldBe None
          }
        }
      }

      "there was a conflict returned when trying to update the mobile number" should {

        lazy val result = {
          mockIndividualAuthorised()
          mockUpdateMobileNumber(vrn, testPrepopMobile)(
            Future(Left(ErrorModel(CONFLICT, "The back end has indicated there is an update already in progress"))))
          controller.updateMobileNumber()(postRequestWithPrepopMobileNumber)
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
          controller.updateMobileNumber()(postRequestWithPrepopMobileNumber)
        }

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "show the internal server error page" in {
          messages(Jsoup.parse(contentAsString(result)).title) shouldBe internalServerErrorTitle
        }
      }
    }

    "there isn't a mobile number in session" should {
      lazy val result = {
        mockIndividualAuthorised()
        controller.updateMobileNumber()(postRequest)
      }

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to the capture mobile number page" in {
        redirectLocation(result) shouldBe Some(routes.CaptureMobileNumberController.show.url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.updateMobileNumber()(postRequestWithPrepopMobileNumber)

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    insolvencyCheck(controller.updateMobileNumber())
  }

  "Calling the removeShow() action" when {

    "there is a validation mobile number in session" should {

      "return 200" in {
        val result = controller.removeShow()(postRequestWithValidationMobileNumber)
        status(result) shouldBe Status.OK
      }
    }

    "there isn't a validation mobile number in session" should {

      lazy val result = controller.removeShow()(postRequest)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the capture mobile page" in {
        redirectLocation(result) shouldBe Some(routes.CaptureMobileNumberController.show.url)
      }
    }

    "the user is not authorised" should {

      "return 403" in {
        val result = {
          mockIndividualWithoutEnrolment()
          controller.removeShow()(postRequest)
        }

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    insolvencyCheck(controller.removeShow())
  }

  "Calling the removeMobileNumber() action" when {

    "there is a validation mobile number in session" when {

      "the form has errors" should {

        lazy val result = controller.removeMobileNumber()(postRequestWithValidationMobileNumber)

        "return 400" in {
          status(result) shouldBe Status.BAD_REQUEST
        }
      }

      "the form is submitted successfully" when {

        "a Yes is submitted" should {

          lazy val result = {
            mockUpdateMobileNumber(vrn, "")(Future(Right(UpdatePPOBSuccess("success"))))
            controller.removeMobileNumber()(
              postRequestWithValidationMobileNumber.withFormUrlEncodedBody("yes_no" -> "yes"))
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the mobileNumber action in ChangeSuccessController" in {
            redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.mobileNumber.url)
          }

          "audit the change mobile number event" in {
            verifyExtendedAudit(ChangedMobileNumberAuditModel(Some(testValidationMobile), "", vrn, isAgent = false, None))
            reset(mockAuditingService)
          }

          "add the successful change key to the session" in {
            session(result).get(SessionKeys.mobileChangeSuccessful) shouldBe Some("true")
          }

          "add the inflight change key to the session" in {
            session(result).get(SessionKeys.inFlightContactDetailsChangeKey) shouldBe Some("true")
          }

          "remove the existing mobile number from the session" in {
            session(result).get(SessionKeys.validationMobileKey) shouldBe None
          }
        }

        "a No is submitted" should {

          lazy val result = controller.removeMobileNumber()(
            postRequestWithValidationMobileNumber.withFormUrlEncodedBody("yes_no" -> "no"))

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the business details page" in {
            redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
          }
        }
      }
    }

    "there isn't a validation mobile number in session" should {

      lazy val result = controller.removeMobileNumber()(postRequest)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the capture mobile page" in {
        redirectLocation(result) shouldBe Some(routes.CaptureMobileNumberController.show.url)
      }
    }

    "the user is not authorised" should {

      "return 403" in {
        val result = {
          mockIndividualWithoutEnrolment()
          controller.removeMobileNumber()(postRequest)
        }

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    insolvencyCheck(controller.removeMobileNumber())
  }
}
