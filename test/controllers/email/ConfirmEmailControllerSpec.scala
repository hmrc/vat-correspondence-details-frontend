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

  "Calling the extractSessionEmail function in ConfirmEmailController" when {

    "there is an authenticated request from a user with an email in session" should {

      "result in an email address being retrieved if there is an email" in {
        mockIndividualAuthorised
        val user = User[AnyContent](vrn, active = true, None)(getRequestWithEmail)

        TestConfirmEmailController.extractSessionEmail(user) shouldBe Some(testEmail)
      }
    }
  }

  "Calling the show action in ConfirmEmailController" when {

    "there is an email in session" should {

      mockIndividualAuthorised
      lazy val result = TestConfirmEmailController.show(getRequestWithEmail)

      "return OK" in {
        status(result) shouldBe Status.OK
      }

      "render the Confirm Email page" which {
        lazy val page = Jsoup.parse(contentAsString(result))

        "has the correct question text" in {
          page.select(".govuk-summary-list__key").text() shouldBe "Email address"
        }

        "has the correct email" in {
          page.select(".govuk-summary-list__value").text()  shouldBe testEmail
        }

        "has the correct change link URL" in {
          page.select(".govuk-summary-list__actions a").attr("href") shouldBe
            controllers.email.routes.CaptureEmailController.show.url
        }

        "has the correct hidden text" in {
          page.select(".govuk-summary-list__actions > a > span:nth-child(2)").text() shouldBe "Change the email address"
        }

        "has the correct form action" in {
          page.select("form").attr("action") shouldBe controllers.email.routes.VerifyPasscodeController.updateEmailAddress.url
        }
      }
    }

    "there isn't an email in session" should {

      lazy val result = {
        mockIndividualAuthorised
        TestConfirmEmailController.show(getRequest)
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
        mockIndividualWithoutEnrolment
        val result = TestConfirmEmailController.show(getRequestWithEmail)

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    insolvencyCheck(TestConfirmEmailController.show())
  }

  "Calling the showContactPref action in ConfirmEmailController" when {

    "there is an email in session" should {

      mockIndividualAuthorised
      lazy val result = TestConfirmEmailController.showContactPref(
        getRequestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper))

      "return OK" in {
        status(result) shouldBe Status.OK
      }

      "render the Confirm Email page" which {

        lazy val page = Jsoup.parse(contentAsString(result))

        "has the correct question text" in {
          page.select(".govuk-summary-list__key").text() shouldBe "Email address"
        }

        "has the correct email" in {
          page.select(".govuk-summary-list__value").text()  shouldBe testEmail
        }

        "has the correct change link URL" in {
          page.select(".govuk-summary-list__actions a").attr("href") shouldBe
            controllers.email.routes.CaptureEmailController.showPrefJourney.url
        }

        "has the correct hidden text" in {
          page.select(".govuk-summary-list__actions > a > span:nth-child(2)").text() shouldBe "Change the email address"
        }

        "has the correct form action" in {
          page.select("form").attr("action") shouldBe
            controllers.email.routes.VerifyPasscodeController.updateContactPrefEmail.url
        }
      }
    }

    "there isn't an email in session" should {

      lazy val result = {
        mockIndividualAuthorised
        TestConfirmEmailController.showContactPref(getRequest.withSession(SessionKeys.currentContactPrefKey -> paper))
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
        mockIndividualWithoutEnrolment
        val result = TestConfirmEmailController.showContactPref(getRequestWithEmail)

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    insolvencyCheck(TestConfirmEmailController.showContactPref())
  }

  "Calling the updateEmailAddress() action in ConfirmEmailController" when {

    "there is a verified email in session" when {

      "the email has been updated successfully" should {

        lazy val result = {
          mockIndividualAuthorised
          mockUpdateEmailAddress(vrn, testEmail)(Future(Right(UpdatePPOBSuccess("success"))))
          TestConfirmEmailController.updateEmailAddress()(postRequestWithEmail)
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

        "remove the validation email from the session" in {
          session(result).get(SessionKeys.validationEmailKey) shouldBe None
        }

        "remove the prepopulation email from the session" in {
          session(result).get(SessionKeys.prepopulationEmailKey) shouldBe None
        }

        "remove the manageVatRequestForFixEmail value from the session" in {
          session(result).get(SessionKeys.manageVatRequestToFixEmail) shouldBe None
        }

        "add the emailChangeSuccessful value to the session" in {
          session(result).get(SessionKeys.emailChangeSuccessful) shouldBe Some("true")
        }

        "add the inflight indicator to the session" in {
          session(result).get(SessionKeys.inFlightContactDetailsChangeKey) shouldBe Some("true")
        }
      }

      "there was a conflict returned when trying to update the email address" should {

        lazy val result = {
          mockIndividualAuthorised
          mockUpdateEmailAddress(vrn, testEmail)(
            Future(Left(ErrorModel(CONFLICT, "The back end has indicated there is an update already in progress"))))
          TestConfirmEmailController.updateEmailAddress()(postRequestWithEmail)
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
          mockIndividualAuthorised
          mockUpdateEmailAddress(vrn, testEmail)(
            Future(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't verify email address"))))
          TestConfirmEmailController.updateEmailAddress()(postRequestWithEmail)
        }

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "show the internal server error page" in {
          messages(Jsoup.parse(contentAsString(result)).title) shouldBe internalServerErrorTitle
        }
      }
    }

    "there is a non-verified email in session" should {

      lazy val result = {
        mockIndividualAuthorised
        mockUpdateEmailAddress(vrn, testEmail)(Future(Right(UpdatePPOBSuccess(""))))
        TestConfirmEmailController.updateEmailAddress()(postRequestWithEmail)
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
        mockIndividualAuthorised
        TestConfirmEmailController.updateEmailAddress()(postRequest)
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
        mockIndividualWithoutEnrolment
        val result = TestConfirmEmailController.updateEmailAddress()(postRequestWithEmail)

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    insolvencyCheck(TestConfirmEmailController.updateEmailAddress())
  }
}
