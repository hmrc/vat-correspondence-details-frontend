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

package controllers.email

import assets.BaseTestConstants._
import controllers.ControllerBaseSpec
import models.User
import models.customerInformation.UpdatePPOBSuccess
import models.errors.ErrorModel
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.http.Status.{CONFLICT, INTERNAL_SERVER_ERROR}
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import views.html.email.ConfirmEmailView

import scala.concurrent.Future

class ConfirmEmailControllerSpec extends ControllerBaseSpec  {

  object TestConfirmEmailController extends ConfirmEmailController(
    mockAuthPredicateComponents,
    mockInflightPPOBPredicate,
    mcc,
    mockErrorHandler,
    mockAuditingService,
    mockVatSubscriptionService,
    injector.instanceOf[ConfirmEmailView],
    mockConfig
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

      "show the Confirm Email page" in {
        mockIndividualAuthorised()
        val result = TestConfirmEmailController.show(requestWithEmail)

        status(result) shouldBe Status.OK
      }
    }

    "there isn't an email in session" should {

      "take the user to enter a new email address" in {
        mockIndividualAuthorised()
        val result = TestConfirmEmailController.show(request)

        status(result) shouldBe Status.SEE_OTHER
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

  "Calling the updateEmailAddress() action in ConfirmEmailController" when {

    "there is a verified email in session" when {

      "the email has been updated successfully" should {

        "show the email changed success page" in {
          mockIndividualAuthorised()
          mockUpdateEmailAddress(vrn, testEmail)(Future(Right(UpdatePPOBSuccess("success"))))
          val result = TestConfirmEmailController.updateEmailAddress()(requestWithEmail)

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/email-address-confirmation")
        }
      }

      "there was a conflict returned when trying to update the email address" should {

        "redirect the user to the manage-vat page " in {
          mockIndividualAuthorised()
          mockUpdateEmailAddress(vrn, testEmail)(
            Future(Left(ErrorModel(CONFLICT, "The back end has indicated there is an update already in progress"))))
          val result = TestConfirmEmailController.updateEmailAddress()(requestWithEmail)

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
        }
      }

      "there was an unexpected error trying to update the email address" should {

        "return an Internal Server Error" in {
          mockIndividualAuthorised()
          mockUpdateEmailAddress(vrn, testEmail)(
            Future(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't verify email address"))))
          val result = TestConfirmEmailController.updateEmailAddress()(requestWithEmail)

          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
          messages(Jsoup.parse(bodyOf(result)).title) shouldBe internalServerErrorTitle
        }
      }
    }

    "there is a non-verified email in session" should {

      "redirect the user to the send email verification page" in {
        mockIndividualAuthorised()
        mockUpdateEmailAddress(vrn, testEmail)(Future(Right(UpdatePPOBSuccess(""))))
        val result = TestConfirmEmailController.updateEmailAddress()(requestWithEmail)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/send-verification")
      }
    }

    "there isn't an email in session" should {

      "take the user to the capture email address page" in {
        mockIndividualAuthorised()
        val result = TestConfirmEmailController.updateEmailAddress()(request)

        status(result) shouldBe Status.SEE_OTHER
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
