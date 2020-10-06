/*
 * Copyright 2020 HM Revenue & Customs
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

import assets.BaseTestConstants.vrn
import common.SessionKeys
import controllers.ControllerBaseSpec
import mocks.MockEmailVerificationService
import models.User
import models.contactPreferences.ContactPreference._
import play.api.http.Status
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class VerifyPasscodeControllerSpec extends ControllerBaseSpec with MockEmailVerificationService {

  object TestVerifyPasscodeController extends VerifyPasscodeController(
    mockEmailVerificationService,
    mockErrorHandler
  )

  lazy val emptyEmailSessionRequest: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(SessionKeys.prepopulationEmailKey -> "")

  "Calling the extractSessionEmail function in VerifyPasscodeController" when {

      "there is an authenticated request from a user with an email in session" should {

        "result in an email address being retrieved if there is an email" in {
          val userWithSession = User[AnyContent](vrn)(requestWithEmail)
          TestVerifyPasscodeController.extractSessionEmail(userWithSession) shouldBe Some(testEmail)
        }
      }
    }

  "Calling the emailShow action in VerifyPasscodeController" when {

    "the emailPinVerification feature switch is enabled" when {

      "there is an email in session" should {

        lazy val result = {
          mockConfig.features.emailPinVerificationEnabled(true)
          TestVerifyPasscodeController.emailShow()(requestWithEmail)
        }

        "return 200 (OK)" in {
          status(result) shouldBe Status.OK
        }
      }

      "there isn't an email in session" should {

        lazy val result = {
          mockConfig.features.emailPinVerificationEnabled(true)
          TestVerifyPasscodeController.emailShow()(emptyEmailSessionRequest)
        }

        "return 303 (SEE_OTHER)" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the capture email page" in {
          redirectLocation(result) shouldBe Some(routes.CaptureEmailController.show().url)
        }
      }
    }

    "the emailPinVerification feature switch is disabled" should {

      lazy val result = {
        mockConfig.features.emailPinVerificationEnabled(false)
        TestVerifyPasscodeController.emailShow()(requestWithEmail)
      }

      "return a NOT FOUND error" in {
        status(result) shouldBe Status.NOT_FOUND
      }
    }
  }

  "Calling the emailSendVerification action in VerifyPasscodeController" when {

    "the emailPinVerification feature switch is enabled" when {

      "there is an email in session" should {

        lazy val result = {
          mockConfig.features.emailPinVerificationEnabled(true)
          mockCreateEmailVerificationRequest(Some(true))
          TestVerifyPasscodeController.emailSendVerification()(requestWithEmail)
        }

        "return OK" in {
          status(result) shouldBe OK
        }

      }

      "there isn't an email in session" should {

        lazy val result = {
          mockConfig.features.emailPinVerificationEnabled(true)
          TestVerifyPasscodeController.emailSendVerification()(emptyEmailSessionRequest)
        }

        "return 303 (SEE_OTHER)" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the capture email route" in {
          redirectLocation(result) shouldBe Some(routes.CaptureEmailController.show().url)
        }
      }

      "the emailPinVerification feature switch is disabled" should {

        lazy val result = {
          mockConfig.features.emailPinVerificationEnabled(false)
          TestVerifyPasscodeController.emailSendVerification()(requestWithEmail)
        }

        "return a NOT FOUND error" in {
          status(result) shouldBe Status.NOT_FOUND
        }
      }
    }
  }

  "Calling the updateEmailAddress action in VerifyPasscodeController" when {

    "the emailPinVerification feature switch is enabled" when {

      "there is an email in session" should {

        lazy val result = {
          mockConfig.features.emailPinVerificationEnabled(true)
          mockCreateEmailVerificationRequest(Some(true))
          TestVerifyPasscodeController.updateEmailAddress()(requestWithEmail)
        }

        "return OK" in {
          status(result) shouldBe OK
        }

      }

      "there isn't an email in session" should {

        lazy val result = {
          mockConfig.features.emailPinVerificationEnabled(true)
          TestVerifyPasscodeController.updateEmailAddress()(emptyEmailSessionRequest)
        }

        "return 303 (SEE_OTHER)" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the capture email route" in {
          redirectLocation(result) shouldBe Some(routes.CaptureEmailController.show().url)
        }
      }

      "the emailPinVerification feature switch is disabled" should {

        lazy val result = {
          mockConfig.features.emailPinVerificationEnabled(false)
          TestVerifyPasscodeController.updateEmailAddress()(requestWithEmail)
        }

        "return a NOT FOUND error" in {
          status(result) shouldBe Status.NOT_FOUND
        }
      }
    }
  }

  "Calling the contactPrefShow action in VerifyPasscodeController" when {

    "the emailPinVerification feature switch is enabled" when {

        "there is an email in session" should {

          lazy val result = {
            mockConfig.features.emailPinVerificationEnabled(true)
            TestVerifyPasscodeController.contactPrefShow()(requestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper))
          }

          "return 200 (OK)" in {
            status(result) shouldBe Status.OK
          }

        }

        "there isn't an email in session" should {

          lazy val result = {
            mockConfig.features.emailPinVerificationEnabled(true)
            TestVerifyPasscodeController.contactPrefShow()(emptyEmailSessionRequest.withSession(SessionKeys.currentContactPrefKey -> paper))
          }

          "return 303 (SEE_OTHER)" in {
            status(result) shouldBe SEE_OTHER
          }

          "redirect to the start of the contact preference journey" in {
            redirectLocation(result) shouldBe Some(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect().url)
          }
        }

        "the emailPinVerification feature switch is disabled" should {

          lazy val result = {
            mockConfig.features.emailPinVerificationEnabled(false)
            TestVerifyPasscodeController.contactPrefShow()(requestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper))
          }

          "return a NOT FOUND error" in {
            status(result) shouldBe Status.NOT_FOUND
          }
        }
    }
  }

  "Calling the contactPrefSendVerification action in VerifyPasscodeController" when {

    "the emailPinVerification feature switch is enabled" when {

      "the email in session is verified" should {

        lazy val result = {
          mockConfig.features.emailPinVerificationEnabled(true)
          mockGetEmailVerificationState(testEmail)(Future.successful(Some(true)))
          TestVerifyPasscodeController.contactPrefSendVerification()(requestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper))
        }

        "return 303 (SEE_OTHER)" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the correct route" in {
          redirectLocation(result) shouldBe Some(routes.VerifyPasscodeController.updateContactPrefEmail().url)
        }
      }

      "the email in session isn't verified" which {

        lazy val result = {
          mockConfig.features.emailPinVerificationEnabled(true)
          mockGetEmailVerificationState(testEmail)(Future.successful(Some(false)))
          mockCreateEmailVerificationRequest(Some(false))
          TestVerifyPasscodeController.contactPrefSendVerification()(requestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper))
        }

        "return 200 (OK)" in {
          status(result) shouldBe Status.OK
        }

      }

      "there isn't an email in session" should {

        lazy val result = {
          mockConfig.features.emailPinVerificationEnabled(true)
          TestVerifyPasscodeController.contactPrefSendVerification()(emptyEmailSessionRequest.withSession(SessionKeys.currentContactPrefKey -> paper))
        }

        "return 303 (SEE_OTHER)" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the contact preference redirect route" in {
          redirectLocation(result) shouldBe Some(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect().url)
        }
      }

      "the emailPinVerification feature switch is disabled" should {

        lazy val result = {
          mockConfig.features.emailPinVerificationEnabled(false)
          TestVerifyPasscodeController.contactPrefSendVerification()(requestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper))
        }

        "return a NOT FOUND error" in {
          status(result) shouldBe Status.NOT_FOUND
        }
      }
    }

    "Calling the updateContactPrefEmail action in VerifyPasscodeController" when {

      "the emailPinVerification feature switch is enabled" when {

        "there is an email in session" should {

          lazy val result = {
            mockConfig.features.emailPinVerificationEnabled(true)
            mockCreateEmailVerificationRequest(Some(true))
            TestVerifyPasscodeController.updateEmailAddress()(requestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper))
          }

          "return OK" in {
            status(result) shouldBe OK
          }

        }

        "there isn't an email in session" should {

          lazy val result = {
            mockConfig.features.emailPinVerificationEnabled(true)
            TestVerifyPasscodeController.updateContactPrefEmail()(emptyEmailSessionRequest.withSession(SessionKeys.currentContactPrefKey -> paper))
          }

          "return 303 (SEE_OTHER)" in {
            status(result) shouldBe SEE_OTHER
          }

          "redirect to the capture email route" in {
            redirectLocation(result) shouldBe Some(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect().url)
          }
        }

        "the emailPinVerification feature switch is disabled" should {

          lazy val result = {
            mockConfig.features.emailPinVerificationEnabled(false)
            TestVerifyPasscodeController.updateEmailAddress()(requestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper))
          }

          "return a NOT FOUND error" in {
            status(result) shouldBe Status.NOT_FOUND
          }
        }
      }
    }
  }
}
