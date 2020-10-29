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
import connectors.httpParsers.VerifyPasscodeHttpParser._
import controllers.ControllerBaseSpec
import mocks.MockEmailVerificationService
import models.User
import models.contactPreferences.ContactPreference._
import models.errors.ErrorModel
import play.api.http.Status
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.email.{PasscodeErrorView, PasscodeView}

class VerifyPasscodeControllerSpec extends ControllerBaseSpec with MockEmailVerificationService {

  object TestVerifyPasscodeController extends VerifyPasscodeController(
    mockEmailVerificationService,
    mockErrorHandler,
    inject[PasscodeView],
    inject[PasscodeErrorView]
  )

  lazy val paperRequestWithEmail: FakeRequest[AnyContentAsEmpty.type] =
    requestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper)

  override def beforeEach(): Unit = {
    super.beforeEach()
    mockConfig.features.emailPinVerificationEnabled(true)
  }

  "Calling the extractSessionEmail function in VerifyPasscodeController" when {

    "the user has an email address in session" should {

      "return the email address" in {
        val userWithSession = User[AnyContent](vrn)(requestWithEmail)
        TestVerifyPasscodeController.extractSessionEmail(userWithSession) shouldBe Some(testEmail)
      }
    }

    "the user does not have an email address in session" should {

      "return None" in {
        val user = User[AnyContent](vrn)(request)
        TestVerifyPasscodeController.extractSessionEmail(user) shouldBe None
      }
    }
  }

  "Calling the emailShow action in VerifyPasscodeController" when {

    "the emailPinVerification feature switch is enabled" when {

      "there is an email in session" should {

        lazy val result = TestVerifyPasscodeController.emailShow()(requestWithEmail)

        "return 200 (OK)" in {
          status(result) shouldBe Status.OK
        }
      }

      "there isn't an email in session" should {

        lazy val result = TestVerifyPasscodeController.emailShow()(request)

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

  "Calling the emailSubmit action" when {

    "the emailPinVerification feature switch is enabled" when {

      "there is an email in session" when {

        "the form is successfully submitted" when {

          "the email verification service returns SuccessfullyVerified" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(SuccessfullyVerified))
              TestVerifyPasscodeController.emailSubmit(requestWithEmail
                .withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 303" in {
              status(result) shouldBe Status.SEE_OTHER
            }

            "redirect to the updateEmailAddress action" in {
              redirectLocation(result) shouldBe Some(routes.VerifyPasscodeController.updateEmailAddress().url)
            }
          }

          "the email verification service returns AlreadyVerified" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(AlreadyVerified))
              TestVerifyPasscodeController.emailSubmit(requestWithEmail
                .withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 303" in {
              status(result) shouldBe Status.SEE_OTHER
            }

            "redirect to the updateEmailAddress action" in {
              redirectLocation(result) shouldBe Some(routes.VerifyPasscodeController.updateEmailAddress().url)
            }
          }

          "the email verification service returns TooManyAttempts" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(TooManyAttempts))
              TestVerifyPasscodeController.emailSubmit(requestWithEmail
                .withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 400" in {
              status(result) shouldBe Status.BAD_REQUEST
            }
          }

          "the email verification service returns PasscodeNotFound" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(PasscodeNotFound))
              TestVerifyPasscodeController.emailSubmit(requestWithEmail
                .withFormUrlEncodedBody("passcode" -> "580008"))
            }

              "return 400" in {
                status(result) shouldBe Status.BAD_REQUEST
              }
            }

          "the email verification service returns IncorrectPasscode" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(IncorrectPasscode))
              TestVerifyPasscodeController.emailSubmit(requestWithEmail.withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 400" in {
              status(result) shouldBe Status.BAD_REQUEST
            }
          }

          "the email verification service returns an unexpected error" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "Err0r")))
              TestVerifyPasscodeController.emailSubmit(requestWithEmail
                .withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 500" in {
              status(result) shouldBe Status.INTERNAL_SERVER_ERROR
            }
          }
        }

        "the form is unsuccessfully submitted" should {

          lazy val result = TestVerifyPasscodeController.emailSubmit(requestWithEmail
            .withFormUrlEncodedBody("passcode" -> "FAIL"))

          "return 400" in {
            status(result) shouldBe Status.BAD_REQUEST
          }
        }
      }

      "there is no email in session" when {

        lazy val result = TestVerifyPasscodeController.emailSubmit(request)

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to the first page in the journey" in {
          redirectLocation(result) shouldBe Some(routes.CaptureEmailController.show().url)
        }
      }
    }

    "the emailPinVerification feature switch is disabled" should {

      lazy val result = {
        mockConfig.features.emailPinVerificationEnabled(false)
        TestVerifyPasscodeController.emailSubmit()(requestWithEmail)
      }

      "return a NOT FOUND error" in {
        status(result) shouldBe Status.NOT_FOUND
      }
    }
  }

  "Calling the emailSendVerification action in VerifyPasscodeController" when {

    "the emailPinVerification feature switch is enabled" when {

      "there is an email in session" should {

        lazy val result = TestVerifyPasscodeController.emailSendVerification()(requestWithEmail)

        "return 200 (OK)" in {
          status(result) shouldBe OK
        }
      }

      "there isn't an email in session" should {

        lazy val result = TestVerifyPasscodeController.emailSendVerification()(request)

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

        lazy val result = TestVerifyPasscodeController.updateEmailAddress()(requestWithEmail)

        "return OK" in {
          status(result) shouldBe OK
        }
      }

      "there isn't an email in session" should {

        lazy val result = TestVerifyPasscodeController.updateEmailAddress()(request)

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

        lazy val result = TestVerifyPasscodeController.contactPrefShow()(paperRequestWithEmail)

        "return 200 (OK)" in {
          status(result) shouldBe Status.OK
        }
      }

      "there isn't an email in session" should {

        lazy val result = TestVerifyPasscodeController.contactPrefShow()(requestWithPaperPref)

        "return 303 (SEE_OTHER)" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the start of the contact preference journey" in {
          redirectLocation(result) shouldBe
            Some(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect().url)
        }
      }

      "the emailPinVerification feature switch is disabled" should {

        lazy val result = {
          mockConfig.features.emailPinVerificationEnabled(false)
          TestVerifyPasscodeController.contactPrefShow()(paperRequestWithEmail)
        }

        "return a NOT FOUND error" in {
          status(result) shouldBe Status.NOT_FOUND
        }
      }
    }
  }

  "Calling the contactPrefSubmit action" when {

    "the emailPinVerification feature switch is enabled" when {

      "there is an email in session" when {

        "the form is successfully submitted" when {

          "the email verification service returns SuccessfullyVerified" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(SuccessfullyVerified))
              TestVerifyPasscodeController.contactPrefSubmit(paperRequestWithEmail
                .withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 303" in {
              status(result) shouldBe Status.SEE_OTHER
            }

            "redirect to the updateContactPrefEmail action" in {
              redirectLocation(result) shouldBe Some(routes.VerifyPasscodeController.updateContactPrefEmail().url)
            }
          }

          "the email verification service returns AlreadyVerified" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(AlreadyVerified))
              TestVerifyPasscodeController.contactPrefSubmit(paperRequestWithEmail
                .withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 303" in {
              status(result) shouldBe Status.SEE_OTHER
            }

            "redirect to the updateContactPrefEmail action" in {
              redirectLocation(result) shouldBe Some(routes.VerifyPasscodeController.updateContactPrefEmail().url)
            }
          }

          "the email verification service returns TooManyAttempts" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(TooManyAttempts))
              TestVerifyPasscodeController.contactPrefSubmit(paperRequestWithEmail
                .withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 400" in {
              status(result) shouldBe Status.BAD_REQUEST
            }
          }

          "the email verification service returns PasscodeNotFound" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(PasscodeNotFound))
              TestVerifyPasscodeController.contactPrefSubmit(paperRequestWithEmail
                .withFormUrlEncodedBody("passcode" -> "580008"))
            }

            "return 400" in {
              status(result) shouldBe Status.BAD_REQUEST
            }
          }

          "the email verification service returns IncorrectPasscode" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(IncorrectPasscode))
              TestVerifyPasscodeController.contactPrefSubmit(paperRequestWithEmail
                .withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 400" in {
              status(result) shouldBe Status.BAD_REQUEST
            }
          }

          "the email verification service returns an unexpected error" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "Err0r")))
              TestVerifyPasscodeController.contactPrefSubmit(paperRequestWithEmail
                .withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 500" in {
              status(result) shouldBe Status.INTERNAL_SERVER_ERROR
            }
          }
        }

        "the form is unsuccessfully submitted" should {

          lazy val result = TestVerifyPasscodeController.contactPrefSubmit(paperRequestWithEmail
            .withFormUrlEncodedBody("passcode" -> "FAIL"))

          "return 400" in {
            status(result) shouldBe Status.BAD_REQUEST
          }
        }
      }

      "there is no email in session" when {

        lazy val result = TestVerifyPasscodeController.contactPrefSubmit(requestWithPaperPref)

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to the first page in the journey" in {
          redirectLocation(result) shouldBe
            Some(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect().url)
        }
      }
    }

    "the emailPinVerification feature switch is disabled" should {

      lazy val result = {
        mockConfig.features.emailPinVerificationEnabled(false)
        TestVerifyPasscodeController.contactPrefSubmit()(paperRequestWithEmail)
      }

      "return a NOT FOUND error" in {
        status(result) shouldBe Status.NOT_FOUND
      }
    }
  }

  "Calling the contactPrefSendVerification action in VerifyPasscodeController" when {

    "the emailPinVerification feature switch is enabled" when {

      "there is an email in session" should {

        lazy val result = TestVerifyPasscodeController.contactPrefSendVerification()(paperRequestWithEmail)

        "return 200 (OK)" in {
          status(result) shouldBe Status.OK
        }
      }

      "there isn't an email in session" should {

        lazy val result = TestVerifyPasscodeController.contactPrefSendVerification()(requestWithPaperPref)

        "return 303 (SEE_OTHER)" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the contact preference redirect route" in {
          redirectLocation(result) shouldBe
            Some(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect().url)
        }
      }

      "the emailPinVerification feature switch is disabled" should {

        lazy val result = {
          mockConfig.features.emailPinVerificationEnabled(false)
          TestVerifyPasscodeController.contactPrefSendVerification()(paperRequestWithEmail)
        }

        "return a NOT FOUND error" in {
          status(result) shouldBe Status.NOT_FOUND
        }
      }
    }

    "Calling the updateContactPrefEmail action in VerifyPasscodeController" when {

      "the emailPinVerification feature switch is enabled" when {

        "there is an email in session" should {

          lazy val result = TestVerifyPasscodeController.updateEmailAddress()(paperRequestWithEmail)

          "return OK" in {
            status(result) shouldBe OK
          }
        }

        "there isn't an email in session" should {

          lazy val result = TestVerifyPasscodeController.updateContactPrefEmail()(requestWithPaperPref)

          "return 303 (SEE_OTHER)" in {
            status(result) shouldBe SEE_OTHER
          }

          "redirect to the capture email route" in {
            redirectLocation(result) shouldBe
              Some(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect().url)
          }
        }

        "the emailPinVerification feature switch is disabled" should {

          lazy val result = {
            mockConfig.features.emailPinVerificationEnabled(false)
            TestVerifyPasscodeController.updateEmailAddress()(paperRequestWithEmail)
          }

          "return a NOT FOUND error" in {
            status(result) shouldBe Status.NOT_FOUND
          }
        }
      }
    }
  }
}
