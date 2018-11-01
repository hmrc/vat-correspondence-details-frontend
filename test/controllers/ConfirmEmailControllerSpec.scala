/*
 * Copyright 2018 HM Revenue & Customs
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

import common.SessionKeys
import mocks.MockVatSubscriptionService
import models.User
import models.customerInformation.UpdateEmailSuccess
import models.errors.ErrorModel
import org.jsoup.Jsoup
import org.scalatest.concurrent.{Waiters, _}
import play.api.http.Status
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import scala.concurrent.Future

class ConfirmEmailControllerSpec extends ControllerBaseSpec with MockVatSubscriptionService with ScalaFutures with Waiters {

  object TestConfirmEmailController extends ConfirmEmailController(
    mockAuthPredicate,
    mockInflightPPOBPredicate,
    messagesApi,
    mockErrorHandler,
    mockConfig,
    mockVatSubscriptionService
  )

  val testVatNumber: String = "999999999"
  val testEmail: String = "test@email.co.uk"

  lazy val testGetRequest = FakeRequest("GET", "/confirm-email")

  "Calling the extractEmail function in ConfirmEmailController" when {
    "there is an authenticated request from a user with an email in session" should {

      "result in an email address being retrieved if there is an email" in {

        mockIndividualAuthorised()

        implicit val request: FakeRequest[AnyContentAsEmpty.type] = testGetRequest.withSession(
         SessionKeys.emailKey -> testEmail)

        val user = User[AnyContent](testVatNumber, active = true, None)(request)
        TestConfirmEmailController.extractSessionEmail(user) shouldBe Some(testEmail)
      }
    }
  }

  "Calling the show action in ConfirmEmailController" when {
    "there is an email in session" should {

      "show the Confirm Email page" in {

        mockIndividualAuthorised()

        val request = testGetRequest.withSession(SessionKeys.emailKey -> testEmail)
        val result = TestConfirmEmailController.show(request)

        status(result) shouldBe Status.OK
      }
    }

    "there isn't an email in session" should {
      "take the user to enter a new email address" in {

        mockIndividualAuthorised()

        val request = testGetRequest.withSession(SessionKeys.emailKey -> "")
        val result = TestConfirmEmailController.show(request)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/change-email-address")
      }
    }

    "the user is not authorised" should {
      "show an internal server error" in {

        mockUnauthorised()

        val request = testGetRequest.withSession(SessionKeys.emailKey -> testEmail)
        val result = TestConfirmEmailController.show(request)
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        Jsoup.parse(bodyOf(result)).title shouldBe "Sorry, we are experiencing technical difficulties - 500"
      }
    }
  }

  "Calling the updateEmailAddress action in ConfirmEmailController" when {

    "there is a verified email in session and the email has been updated successfully" should {
      "show the email changed success page" in {

        mockIndividualAuthorised()
        mockUpdateEmailAddress(testVatNumber, testEmail)(Future(Right(UpdateEmailSuccess("success"))))
        val request = testGetRequest.withSession(SessionKeys.emailKey -> testEmail)
        val result = TestConfirmEmailController.updateEmailAddress(request)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/email-change-success")

      }
    }

    "there is a non-verified email in session" should {
      "redirect the user to the send email verification pagee" in {

        mockIndividualAuthorised()
        mockUpdateEmailAddress(testVatNumber, testEmail)(Future(Right(UpdateEmailSuccess(""))))
        val request = testGetRequest.withSession(SessionKeys.emailKey -> testEmail)
        val result = TestConfirmEmailController.updateEmailAddress(request)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/send-verification")

      }
    }

    "there is a verified email in session but email could not be updated because the user could not be found" should {
      "throw an Internal Server Exception" in {

        mockIndividualAuthorised()
        mockUpdateEmailAddress(testVatNumber, testEmail)(Future(Left(ErrorModel(NOT_FOUND, "Couldn't find a user with VRN provided"))))
        val request = testGetRequest.withSession(SessionKeys.emailKey -> testEmail)
        val result = TestConfirmEmailController.updateEmailAddress(request)

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        Jsoup.parse(bodyOf(result)).title shouldBe "Sorry, we are experiencing technical difficulties - 500"
      }
    }

    "there is a verified email in session but there was an error trying to update the email address" should {
      "throw an Internal Server Exception" in {

        mockIndividualAuthorised()
        mockUpdateEmailAddress(testVatNumber, testEmail)(Future(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't verify email address"))))
        val request = testGetRequest.withSession(SessionKeys.emailKey -> testEmail)
        val result = TestConfirmEmailController.updateEmailAddress(request)

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        Jsoup.parse(bodyOf(result)).title shouldBe "Sorry, we are experiencing technical difficulties - 500"
      }
    }

    "there isn't an email in session" should {
      "take the user to the capture email address page" in {

        mockIndividualAuthorised()

        val request = testGetRequest
        val result = TestConfirmEmailController.updateEmailAddress(request)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/change-email-address")

      }
    }

    "the user is not authorised" should {
      "show an internal server error" in {

        mockUnauthorised()

        val request = testGetRequest.withSession(SessionKeys.emailKey -> testEmail)
        val result = TestConfirmEmailController.updateEmailAddress(request)

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        Jsoup.parse(bodyOf(result)).title shouldBe "Sorry, we are experiencing technical difficulties - 500"
      }
    }
  }
}
