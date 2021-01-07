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

package controllers.email

import assets.BaseTestConstants._
import audit.models.ChangedEmailAddressAuditModel
import common.SessionKeys
import controllers.ControllerBaseSpec
import models.User
import models.customerInformation.UpdatePPOBSuccess
import models.errors.ErrorModel
import org.jsoup.Jsoup
import org.mockito.Mockito.reset
import play.api.http.Status
import play.api.http.Status.{CONFLICT, INTERNAL_SERVER_ERROR}
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import views.html.templates.CheckYourAnswersView
import models.contactPreferences.ContactPreference._

import scala.concurrent.Future

class ConfirmEmailControllerSpec extends ControllerBaseSpec  {

  object TestConfirmEmailController extends ConfirmEmailController(
    mockErrorHandler,
    mockAuditingService,
    mockVatSubscriptionService,
    injector.instanceOf[CheckYourAnswersView]
  )

  "Calling the extractEmail function in ConfirmEmailController" when {

    "there is an authenticated request from a user with an email in session" should {

      "result in an email address being retrieved if there is an email" in {
        mockIndividualAuthorised()
        val user = User[AnyContent](vrn, active = true, None)(requestWithEmail)

        TestConfirmEmailController.extractSessionEmail(user) shouldBe Some(testEmail)
      }
    }
  }

  "Calling the show action in ConfirmEmailController" when {

    "there is an email in session" should {

      mockIndividualAuthorised()
      lazy val result = TestConfirmEmailController.show(requestWithEmail)

      "return OK" in {
        status(result) shouldBe Status.OK
      }

      "render the Confirm Email page" which {
        lazy val page = Jsoup.parse(bodyOf(result))

        "has the correct question text" in {
          page.select(".cya-question").text() shouldBe "Email address"
        }

        "has the correct email" in {
          page.select(".cya-answer").text()  shouldBe testEmail
        }

        "has the correct change link URL" in {
          page.select(".cya-change a").attr("href") shouldBe controllers.email.routes.CaptureEmailController.show().url
        }

        "has the correct hidden text" in {
          page.select(".cya-change a").attr("aria-label") shouldBe "Change the email address"
        }

        "has the correct continue URL" in {
          page.select(".button").attr("href") shouldBe controllers.email.routes.ConfirmEmailController.updateEmailAddress().url
        }
      }
    }

    "there isn't an email in session" should {

      lazy val result = {
        mockIndividualAuthorised()
        TestConfirmEmailController.show(request)
      }

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to enter a new email address" in {
        redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/change-email-address")
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = TestConfirmEmailController.show(requestWithEmail)

        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }

  "Calling the showContactPref action in ConfirmEmailController" when {

    "there is an email in session" should {

      mockIndividualAuthorised()
      lazy val result = TestConfirmEmailController.showContactPref(requestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper))

      "return OK" in {
        status(result) shouldBe Status.OK
      }

      "render the Confirm Email page" which {

        lazy val page = Jsoup.parse(bodyOf(result))

        "has the correct question text" in {
          page.select(".cya-question").text() shouldBe "Email address"
        }

        "has the correct email" in {
          page.select(".cya-answer").text()  shouldBe testEmail
        }

        "has the correct change link URL" in {
          page.select(".cya-change a").attr("href") shouldBe
            controllers.email.routes.CaptureEmailController.showPrefJourney().url
        }

        "has the correct hidden text" in {
          page.select(".cya-change a").attr("aria-label") shouldBe "Change the email address"
        }

        "has the correct continue URL" in {
          page.select(".button").attr("href") shouldBe
            controllers.email.routes.VerifyEmailController.updateContactPrefEmail().url
        }
      }
    }

    "there isn't an email in session" should {

      lazy val result = {
        mockIndividualAuthorised()
        TestConfirmEmailController.showContactPref(request.withSession(SessionKeys.currentContactPrefKey -> paper))
      }

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to enter a new email address" in {
        redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/change-email-address")
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = TestConfirmEmailController.showContactPref(requestWithEmail)

        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }

  "Calling the updateEmailAddress() action in ConfirmEmailController" when {

    "there is a verified email in session" when {

      "the email has been updated successfully" should {

        lazy val result = {
          mockIndividualAuthorised()
          mockUpdateEmailAddress(vrn, testEmail)(Future(Right(UpdatePPOBSuccess("success"))))
          TestConfirmEmailController.updateEmailAddress()(requestWithEmail)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "audit the email change event" in {
          verifyExtendedAudit(ChangedEmailAddressAuditModel(None, testEmail, vrn, isAgent = false, None))
          reset(mockAuditingService)
        }

        "redirect to the email changed success page" in {
          redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/email-address-confirmation")
        }
      }

      "there was a conflict returned when trying to update the email address" should {

        lazy val result = {
          mockIndividualAuthorised()
          mockUpdateEmailAddress(vrn, testEmail)(
            Future(Left(ErrorModel(CONFLICT, "The back end has indicated there is an update already in progress"))))
          TestConfirmEmailController.updateEmailAddress()(requestWithEmail)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect the user to the manage-vat page " in {
          redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
        }
      }

      "there was an unexpected error trying to update the email address" should {

        lazy val result = {
          mockIndividualAuthorised()
          mockUpdateEmailAddress(vrn, testEmail)(
            Future(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't verify email address"))))
          TestConfirmEmailController.updateEmailAddress()(requestWithEmail)
        }

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "show the internal server error page" in {
          messages(Jsoup.parse(bodyOf(result)).title) shouldBe internalServerErrorTitle
        }
      }
    }

    "there is a non-verified email in session" should {

      lazy val result = {
        mockIndividualAuthorised()
        mockUpdateEmailAddress(vrn, testEmail)(Future(Right(UpdatePPOBSuccess(""))))
        TestConfirmEmailController.updateEmailAddress()(requestWithEmail)
      }

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to the send email verification page" in {
        redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/send-verification")
      }
    }

    "there isn't an email in session" should {
      lazy val result = {
        mockIndividualAuthorised()
        TestConfirmEmailController.updateEmailAddress()(request)
      }

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to the capture email address page" in {
        redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/change-email-address")
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = TestConfirmEmailController.updateEmailAddress()(requestWithEmail)

        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }
}
