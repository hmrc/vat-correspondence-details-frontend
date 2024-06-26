/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.website

import assets.BaseTestConstants._
import audit.models.ChangedWebsiteAddressAuditModel
import common.SessionKeys
import controllers.ControllerBaseSpec
import models.customerInformation.UpdatePPOBSuccess
import models.errors.ErrorModel
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.http.Status.{CONFLICT, INTERNAL_SERVER_ERROR}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.templates.CheckYourAnswersView
import views.html.website.ConfirmRemoveWebsiteView

import scala.concurrent.Future

class ConfirmWebsiteControllerSpec extends ControllerBaseSpec  {

  val controller = new ConfirmWebsiteController(
    mockErrorHandler,
    mockVatSubscriptionService,
    inject[CheckYourAnswersView],
    inject[ConfirmRemoveWebsiteView],
    mockAuditingService
  )

  lazy val getRequestWithValidationWebsite: FakeRequest[AnyContentAsEmpty.type] =
    getRequest.withSession(SessionKeys.validationWebsiteKey -> testWebsite)

  lazy val postRequestWithValidationWebsite: FakeRequest[AnyContentAsEmpty.type] =
    postRequest.withSession(SessionKeys.validationWebsiteKey -> testWebsite)

  "Calling the show action in ConfirmWebsiteController" when {

    "there is a website in session" should {

      "show the Confirm Website page" in {
        mockIndividualAuthorised
        val result = controller.show(getRequestWithWebsite)

        status(result) shouldBe Status.OK
      }
    }

    "there isn't a website in session" should {

      lazy val result = {
        mockIndividualAuthorised
        controller.show(getRequest)
      }

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to enter a new website" in {
        redirectLocation(result) shouldBe Some(controllers.website.routes.CaptureWebsiteController.show.url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment
        val result = controller.show(getRequestWithWebsite)

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    insolvencyCheck(controller.show)
  }

  "Calling the updateWebsite() action in ConfirmWebsiteController" when {

    "there is a website in session" when {

      "the website has been updated successfully" should {

        lazy val result = {
          mockIndividualAuthorised
          mockUpdateWebsite(vrn, testWebsite)(Future(Right(UpdatePPOBSuccess("success"))))
          controller.updateWebsite()(postRequestWithWebsite)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "audit the website change event" in {
          verifyExtendedAudit(ChangedWebsiteAddressAuditModel(Some(testWebsite), testWebsite, vrn, None))
        }

        "redirect to the website changed success page" in {
          redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.websiteAddress.url)
        }

        "add the successful change key to the session" in {
          session(result).get(SessionKeys.websiteChangeSuccessful) shouldBe Some("true")
        }

        "add the inflight change key to the session" in {
          session(result).get(SessionKeys.inFlightContactDetailsChangeKey) shouldBe Some("true")
        }

        "remove the existing website from the session" in {
          session(result).get(SessionKeys.validationWebsiteKey) shouldBe None
        }

        "remove the website prepop value from the session" in {
          session(result).get(SessionKeys.prepopulationWebsiteKey) shouldBe None
        }
      }

      "there was a conflict returned when trying to update the website" should {

        lazy val result = {
          mockIndividualAuthorised
          mockUpdateWebsite(vrn, testWebsite)(
            Future(Left(ErrorModel(CONFLICT, "The back end has indicated there is an update already in progress"))))
          controller.updateWebsite()(postRequestWithWebsite)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect the user to the manage-vat overview page" in {
          redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
        }
      }

      "there was an unexpected error trying to update the website" should {

        lazy val result = {
          mockIndividualAuthorised
          mockUpdateWebsite(vrn, testWebsite)(
            Future(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't verify website"))))
          controller.updateWebsite()(postRequestWithWebsite)
        }

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "show the internal server error page" in {
          messages(Jsoup.parse(contentAsString(result)).title) shouldBe internalServerErrorTitle
        }
      }
    }

    "there isn't a website in session" should {

      lazy val result = {
        mockIndividualAuthorised
        controller.updateWebsite()(postRequest)
      }

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to the capture website page" in {
        redirectLocation(result) shouldBe Some(controllers.website.routes.CaptureWebsiteController.show.url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment
        val result = controller.updateWebsite()(postRequestWithWebsite)

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    insolvencyCheck(controller.updateWebsite())
  }

  "Calling the removeShow() action" when {

    "there is a website address in session" should {

      "return 200" in {
        val result = controller.removeShow()(postRequestWithValidationWebsite)
        status(result) shouldBe Status.OK
      }
    }

    "there isn't a website address in session" should {

      lazy val result = controller.removeShow()(postRequest)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the capture website page" in {
        redirectLocation(result) shouldBe Some(routes.CaptureWebsiteController.show.url)
      }
    }

    "the user is not authorised" should {

      "return 403" in {
        val result = {
          mockIndividualWithoutEnrolment
          controller.removeShow()(postRequestWithValidationWebsite)
        }

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    insolvencyCheck(controller.removeShow())
  }

  "Calling the removeWebsiteAddress() action" when {

    "there is a validation website address in session" when {

      "the form has errors" should {

        lazy val result = controller.removeWebsiteAddress()(postRequestWithValidationWebsite)

        "return 400" in {
          status(result) shouldBe Status.BAD_REQUEST
        }
      }

      "the form is submitted successfully" when {

        "the Yes option is submitted" should {

          lazy val result = {
            mockUpdateWebsite(vrn, "")(Future(Right(UpdatePPOBSuccess("success"))))
            controller.removeWebsiteAddress()(postRequestWithValidationWebsite
              .withFormUrlEncodedBody("yes_no" -> "yes"))
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the websiteAddress action in ChangeSuccessController" in {
            redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.websiteAddress.url)
          }

          "audit the website change event" in {
            verifyExtendedAudit(ChangedWebsiteAddressAuditModel(Some(testWebsite), "", vrn, None))
          }

          "add the successful change key to the session" in {
            session(result).get(SessionKeys.websiteChangeSuccessful) shouldBe Some("true")
          }

          "add the inflight change key to the session" in {
            session(result).get(SessionKeys.inFlightContactDetailsChangeKey) shouldBe Some("true")
          }

          "remove the existing website from the session" in {
            session(result).get(SessionKeys.validationWebsiteKey) shouldBe None
          }
        }

        "the No option is submitted" should {

          lazy val result = controller.removeWebsiteAddress()(postRequestWithValidationWebsite
            .withFormUrlEncodedBody("yes_no" -> "no"))

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the business details page" in {
            redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
          }
        }

        "there isn't a validation website address in session" should {

          lazy val result = controller.removeWebsiteAddress()(postRequest)

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the capture website page" in {
            redirectLocation(result) shouldBe Some(routes.CaptureWebsiteController.show.url)
          }
        }

        "the user is not authorised" should {

          "return 403" in {
            val result = {
              mockIndividualWithoutEnrolment
              controller.removeWebsiteAddress()(postRequestWithValidationWebsite)
            }

            status(result) shouldBe Status.FORBIDDEN
          }
        }

        insolvencyCheck(controller.removeWebsiteAddress())
      }
    }
  }
}
