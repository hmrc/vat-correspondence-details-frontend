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

package controllers

import common.SessionKeys
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.test.Helpers._
import mocks.MockEmailVerificationService
import models.User
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import views.html.VerifyEmailView

class VerifyEmailControllerSpec extends ControllerBaseSpec with MockEmailVerificationService {

  object TestVerifyEmailController extends VerifyEmailController(
    mockAuthPredicate,
    mockInflightPPOBPredicate,
    mcc,
    mockEmailVerificationService,
    mockErrorHandler,
    injector.instanceOf[VerifyEmailView],
    mockConfig
  )

  val testVatNumber: String = "999999999"
  val testContinueUrl: String = "/someReturnUrl/verified"

  lazy val testEmailSessionRequest: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(SessionKeys.emailKey -> testEmail)
  lazy val emptyEmailSessionRequest: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(SessionKeys.emailKey -> "")


  "Calling the extractSessionEmail function in VerifyEmailController" when {

    "there is an authenticated request from a user with an email in session" should {

      "result in an email address being retrieved if there is an email" in {
        val userWithSession = User[AnyContent](testVatNumber)(testEmailSessionRequest)
        TestVerifyEmailController.extractSessionEmail(userWithSession) shouldBe Some(testEmail)
      }
    }
  }

  "Calling the show action in VerifyEmailController" when {

    "there is an email in session" should {

      lazy val result = {
        mockIndividualAuthorised()
        TestVerifyEmailController.show()(testEmailSessionRequest)
      }

      "return 200 (OK)" in {
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "there isn't an email in session" should {

      lazy val result = {
        mockIndividualAuthorised()
        TestVerifyEmailController.show()(emptyEmailSessionRequest)
      }

      "return 303 (SEE_OTHER)" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect to the capture email page" in {
        redirectLocation(result) shouldBe Some(routes.CaptureEmailController.show().url)
      }
    }

    "the user is not authorised" should {

      lazy val result = {
        mockUnauthorised()
        TestVerifyEmailController.show()(testEmailSessionRequest)
      }

      "return 500 (INTERNAL_SERVER_ERROR)" in {
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "show the technical difficulties page" in {
        Jsoup.parse(bodyOf(result)).title shouldBe "Sorry, we are experiencing technical difficulties - 500"
      }
    }
  }

  "Calling the sendVerification action in VerifyEmailController" when {

    "there is an email in session and the email request is successfully created" should {

      lazy val result = {
        mockIndividualAuthorised()
        mockCreateEmailVerificationRequest(Some(true))
        TestVerifyEmailController.sendVerification()(testEmailSessionRequest)
      }

      "return 303 (SEE_OTHER)" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect to the verify your email page" in {
        redirectLocation(result) shouldBe Some(routes.VerifyEmailController.show().url)
      }
    }

    "there is an email in session and the email request is not created as already verified" should {

      lazy val result = {
        mockIndividualAuthorised()
        mockCreateEmailVerificationRequest(Some(false))
        TestVerifyEmailController.sendVerification()(testEmailSessionRequest)
      }

      "return 303 (SEE_OTHER)" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect to the update email address route" in {
        redirectLocation(result) shouldBe Some(routes.ConfirmEmailController.updateEmailAddress().url)
      }
    }


    "there is an email in session and the email request returned an unexpected error" should {

      lazy val result = {
        mockIndividualAuthorised()
        mockCreateEmailVerificationRequest(None)
        TestVerifyEmailController.sendVerification()(testEmailSessionRequest)
      }

      "return 500 (INTERNAL_SERVER_ERROR)" in {
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "there isn't an email in session" should {

      lazy val result = {
        mockIndividualAuthorised()
        TestVerifyEmailController.sendVerification()(emptyEmailSessionRequest)
      }

      "return 303 (SEE_OTHER)" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect to the capture email route" in {
        redirectLocation(result) shouldBe Some(routes.CaptureEmailController.show().url)
      }
    }

    "the user is not authorised" should {

      lazy val result = {
        mockUnauthorised()
        TestVerifyEmailController.sendVerification()(testEmailSessionRequest)
      }

      "return 500 (INTERNAL_SERVER_ERROR)" in {
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "show the internal server error page" in {
        messages(Jsoup.parse(bodyOf(result)).title) shouldBe "Sorry, we are experiencing technical difficulties - 500"
      }
    }
  }
}
