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

package controllers.website

import assets.BaseTestConstants._
import audit.models.ChangedWebsiteAddressAuditModel
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

class ConfirmWebsiteControllerSpec extends ControllerBaseSpec  {

  val controller = new ConfirmWebsiteController(
    mockErrorHandler,
    mockVatSubscriptionService,
    injector.instanceOf[CheckYourAnswersView],
    mockAuditingService
  )

  "Calling the show action in ConfirmWebsiteController" when {

    "there is a website in session" should {

      "show the Confirm Website page" in {
        mockIndividualAuthorised()
        val result = controller.show(requestWithWebsite)

        status(result) shouldBe Status.OK
      }
    }

    "there isn't a website in session" should {

      lazy val result = {
        mockIndividualAuthorised()
        controller.show(request)
      }

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to enter a new website" in {
        redirectLocation(result) shouldBe Some(controllers.website.routes.CaptureWebsiteController.show().url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.show(requestWithWebsite)

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    "the change contact details feature is disabled" should {

      "present the server error page" in {
        mockConfig.features.changeContactDetailsEnabled(false)
        mockIndividualAuthorised()

        val result = controller.show(request)

        status(result) shouldBe Status.SEE_OTHER
        mockConfig.features.changeContactDetailsEnabled(true)
      }
    }

    insolvencyCheck(controller.show)
  }

  "Calling the updateWebsite() action in ConfirmWebsiteController" when {

    "there is a website in session" when {

      "the website has been updated successfully" should {

        lazy val result = {
          mockIndividualAuthorised()
          mockUpdateWebsite(vrn, testWebsite)(Future(Right(UpdatePPOBSuccess("success"))))
          controller.updateWebsite()(requestWithWebsite)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "audit the website change event" in {
          verifyExtendedAudit(ChangedWebsiteAddressAuditModel(None, testWebsite, vrn, isAgent = false, None))
          reset(mockAuditingService)
        }

        "redirect to the website changed success page" in {
          redirectLocation(result) shouldBe Some(controllers.routes.ChangeSuccessController.websiteAddress().url)
        }
      }

      "there was a conflict returned when trying to update the website" should {

        lazy val result = {
          mockIndividualAuthorised()
          mockUpdateWebsite(vrn, testWebsite)(
            Future(Left(ErrorModel(CONFLICT, "The back end has indicated there is an update already in progress"))))
          controller.updateWebsite()(requestWithWebsite)
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
          mockIndividualAuthorised()
          mockUpdateWebsite(vrn, testWebsite)(
            Future(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't verify website"))))
          controller.updateWebsite()(requestWithWebsite)
        }

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "show the internal server error page" in {
          messages(Jsoup.parse(bodyOf(result)).title) shouldBe internalServerErrorTitle
        }
      }
    }

    "there isn't a website in session" should {

      lazy val result = {
        mockIndividualAuthorised()
        controller.updateWebsite()(request)
      }

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to the capture website page" in {
        redirectLocation(result) shouldBe Some(controllers.website.routes.CaptureWebsiteController.show().url)
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = controller.updateWebsite()(requestWithWebsite)

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    insolvencyCheck(controller.updateWebsite())
  }
}
