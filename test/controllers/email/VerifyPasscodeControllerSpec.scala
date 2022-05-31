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

package controllers.email

import assets.BaseTestConstants.vrn
import common.SessionKeys
import connectors.httpParsers.VerifyPasscodeHttpParser._
import controllers.ControllerBaseSpec
import mocks.MockEmailVerificationService
import models.User
import models.contactPreferences.ContactPreference._
import models.customerInformation.{UpdateEmailSuccess, UpdatePPOBSuccess}
import models.errors.ErrorModel
import play.api.http.Status
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.email.{PasscodeErrorView, PasscodeView}

import scala.concurrent.Future

class VerifyPasscodeControllerSpec extends ControllerBaseSpec with MockEmailVerificationService {

  object TestVerifyPasscodeController extends VerifyPasscodeController(
    mockEmailVerificationService,
    mockErrorHandler,
    inject[PasscodeView],
    inject[PasscodeErrorView],
    mockVatSubscriptionService,
    mockAuditingService
  )

  lazy val paperGetRequestWithEmail: FakeRequest[AnyContentAsEmpty.type] =
    getRequestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper)

  lazy val paperPostRequestWithEmail: FakeRequest[AnyContentAsEmpty.type] =
    postRequestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper)

  "Calling the extractSessionEmail function in VerifyPasscodeController" when {

    "the user has an email address in session" should {

      "return the email address" in {
        val userWithSession = User[AnyContent](vrn)(getRequestWithEmail)
        TestVerifyPasscodeController.extractSessionEmail(userWithSession) shouldBe Some(testEmail)
      }
    }

    "the user does not have an email address in session" should {

      "return None" in {
        val user = User[AnyContent](vrn)(getRequest)
        TestVerifyPasscodeController.extractSessionEmail(user) shouldBe None
      }
    }
  }

  "Calling the emailShow action in VerifyPasscodeController" when {

    "the emailPinVerification feature switch is enabled" when {

      "there is an email in session" should {

        lazy val result = TestVerifyPasscodeController.emailShow()(getRequestWithEmail)

        "return 200 (OK)" in {
          status(result) shouldBe Status.OK
        }
      }

      "there isn't an email in session" should {

        lazy val result = TestVerifyPasscodeController.emailShow()(getRequest)

        "return 303 (SEE_OTHER)" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the capture email page" in {
          redirectLocation(result) shouldBe Some(routes.CaptureEmailController.show.url)
        }
      }
    }

    insolvencyCheck(TestVerifyPasscodeController.emailShow())
  }

  "Calling the emailSubmit action" when {

    "the emailPinVerification feature switch is enabled" when {

      "there is an email in session" when {

        "the form is successfully submitted" when {

          "the email verification service returns SuccessfullyVerified" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(SuccessfullyVerified))
              TestVerifyPasscodeController.emailSubmit(postRequestWithEmail
                .withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 303" in {
              status(result) shouldBe Status.SEE_OTHER
            }

            "redirect to the updateEmailAddress action" in {
              redirectLocation(result) shouldBe Some(routes.VerifyPasscodeController.updateEmailAddress.url)
            }
          }

          "the email verification service returns AlreadyVerified" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(AlreadyVerified))
              TestVerifyPasscodeController.emailSubmit(postRequestWithEmail
                .withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 303" in {
              status(result) shouldBe Status.SEE_OTHER
            }

            "redirect to the updateEmailAddress action" in {
              redirectLocation(result) shouldBe Some(routes.VerifyPasscodeController.updateEmailAddress.url)
            }
          }

          "the email verification service returns TooManyAttempts" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(TooManyAttempts))
              TestVerifyPasscodeController.emailSubmit(postRequestWithEmail
                .withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 400" in {
              status(result) shouldBe Status.BAD_REQUEST
            }
          }

          "the email verification service returns PasscodeNotFound" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(PasscodeNotFound))
              TestVerifyPasscodeController.emailSubmit(postRequestWithEmail
                .withFormUrlEncodedBody("passcode" -> "580008"))
            }

              "return 400" in {
                status(result) shouldBe Status.BAD_REQUEST
              }
            }

          "the email verification service returns IncorrectPasscode" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(IncorrectPasscode))
              TestVerifyPasscodeController.emailSubmit(postRequestWithEmail.withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 400" in {
              status(result) shouldBe Status.BAD_REQUEST
            }
          }

          "the email verification service returns an unexpected error" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "Err0r")))
              TestVerifyPasscodeController.emailSubmit(postRequestWithEmail
                .withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 500" in {
              status(result) shouldBe Status.INTERNAL_SERVER_ERROR
            }
          }
        }

        "the form is unsuccessfully submitted" should {

          lazy val result = TestVerifyPasscodeController.emailSubmit(postRequestWithEmail
            .withFormUrlEncodedBody("passcode" -> "FAIL"))

          "return 400" in {
            status(result) shouldBe Status.BAD_REQUEST
          }
        }
      }

      "there is no email in session" when {

        lazy val result = TestVerifyPasscodeController.emailSubmit(postRequest)

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to the first page in the journey" in {
          redirectLocation(result) shouldBe Some(routes.CaptureEmailController.show.url)
        }
      }
    }
    insolvencyCheck(TestVerifyPasscodeController.emailSubmit())
  }

  "Calling the emailSendVerification action in VerifyPasscodeController" when {

    "the emailPinVerification feature switch is enabled" when {

      "there is an email in session" when {

        "the email is not already verified" should {

          lazy val result = {
            mockCreateEmailPasscodeRequest(Some(true))
            TestVerifyPasscodeController.emailSendVerification()(getRequestWithEmail)
          }

          "return 303 (SEE_OTHER)" in {
            status(result) shouldBe SEE_OTHER
          }

          "redirect to the emailShow route" in {
            redirectLocation(result) shouldBe Some(routes.VerifyPasscodeController.emailShow.url)
          }
        }

        "the email is already verified" should {

          lazy val result = {
            mockCreateEmailPasscodeRequest(Some(false))
            TestVerifyPasscodeController.emailSendVerification()(getRequestWithEmail)
          }

          "return 303 (SEE_OTHER)" in {
            status(result) shouldBe SEE_OTHER
          }

          "redirect to the capture email page" in {
            redirectLocation(result) shouldBe Some(routes.VerifyPasscodeController.updateEmailAddress.url)
          }
        }

        "anything else, such as a None, is returned by the email verification service" should {

          lazy val result = {
            mockCreateEmailPasscodeRequest(None)
            TestVerifyPasscodeController.emailSendVerification()(getRequestWithEmail)
          }

          "return 500 (INTERNAL_SERVER_ERROR)" in {
            status(result) shouldBe INTERNAL_SERVER_ERROR
          }
        }
      }

      "there isn't an email in session" should {

        lazy val result = TestVerifyPasscodeController.emailSendVerification()(getRequest)

        "return 303 (SEE_OTHER)" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the capture email route" in {
          redirectLocation(result) shouldBe Some(routes.CaptureEmailController.show.url)
        }
      }
    }

    insolvencyCheck(TestVerifyPasscodeController.emailSendVerification())
  }

  "Calling the updateEmailAddress action in VerifyPasscodeController" when {

    "the emailPinVerification feature switch is enabled" when {

      "there is an email in session" when {

        "an UpdatePPOBSuccess comes back with a non-empty message" should {

          lazy val result = {
            mockUpdateEmail()(Future(Right(UpdatePPOBSuccess("success"))))
            TestVerifyPasscodeController.updateEmailAddress()(postRequestWithEmail)
          }

          "return SEE_OTHER" in {
            status(result) shouldBe SEE_OTHER
          }

          "have the correct redirect location" in {
            redirectLocation(result) shouldBe Some(routes.EmailChangeSuccessController.show.url)
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

        "an UpdatePPOBSuccess comes back with an empty message" should {

          lazy val result = {
            mockUpdateEmail()(Future(Right(UpdatePPOBSuccess(""))))
            TestVerifyPasscodeController.updateEmailAddress()(postRequestWithEmail)
          }

          "return SEE_OTHER" in {
            status(result) shouldBe SEE_OTHER
          }

          "have the correct redirect location" in {
            redirectLocation(result) shouldBe Some(routes.VerifyPasscodeController.emailSendVerification.url)
          }
        }

        "a CONFLICT error comes back" should {

          lazy val result = {
            mockUpdateEmail()(Future(Left(ErrorModel(CONFLICT, ""))))
            TestVerifyPasscodeController.updateEmailAddress()(postRequestWithEmail)
          }

          "return SEE_OTHER" in {
            status(result) shouldBe SEE_OTHER
          }

          "have the correct redirect location" in {
            redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
          }
        }

        "an unexpected error comes back" should {
          lazy val result = {
            mockUpdateEmail()(Future(Left(ErrorModel(1, ""))))
            TestVerifyPasscodeController.updateEmailAddress()(postRequestWithEmail)
          }

          "produce an internal server error" in {
            status(result) shouldBe INTERNAL_SERVER_ERROR
          }
        }
      }

      "there isn't an email in session" should {

        lazy val result = TestVerifyPasscodeController.updateEmailAddress()(postRequest)

        "return 303 (SEE_OTHER)" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the capture email route" in {
          redirectLocation(result) shouldBe Some(routes.CaptureEmailController.show.url)
        }
      }
    }

    insolvencyCheck(TestVerifyPasscodeController.updateEmailAddress())
  }

  "Calling the contactPrefShow action in VerifyPasscodeController" when {

    "the emailPinVerification feature switch is enabled" when {

      "there is an email in session" should {

        lazy val result = TestVerifyPasscodeController.contactPrefShow()(paperGetRequestWithEmail)

        "return 200 (OK)" in {
          status(result) shouldBe Status.OK
        }
      }

      "there isn't an email in session" should {

        lazy val result = TestVerifyPasscodeController.contactPrefShow()(getRequestWithPaperPref)

        "return 303 (SEE_OTHER)" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the start of the contact preference journey" in {
          redirectLocation(result) shouldBe
            Some(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect.url)
        }
      }
    }

    insolvencyCheck(TestVerifyPasscodeController.contactPrefShow())
  }

  "Calling the contactPrefSubmit action" when {

    "the emailPinVerification feature switch is enabled" when {

      "there is an email in session" when {

        "the form is successfully submitted" when {

          "the email verification service returns SuccessfullyVerified" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(SuccessfullyVerified))
              TestVerifyPasscodeController.contactPrefSubmit(paperPostRequestWithEmail
                .withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 303" in {
              status(result) shouldBe Status.SEE_OTHER
            }

            "redirect to the updateContactPrefEmail action" in {
              redirectLocation(result) shouldBe Some(routes.VerifyPasscodeController.updateContactPrefEmail.url)
            }
          }

          "the email verification service returns AlreadyVerified" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(AlreadyVerified))
              TestVerifyPasscodeController.contactPrefSubmit(paperPostRequestWithEmail
                .withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 303" in {
              status(result) shouldBe Status.SEE_OTHER
            }

            "redirect to the updateContactPrefEmail action" in {
              redirectLocation(result) shouldBe Some(routes.VerifyPasscodeController.updateContactPrefEmail.url)
            }
          }

          "the email verification service returns TooManyAttempts" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(TooManyAttempts))
              TestVerifyPasscodeController.contactPrefSubmit(paperPostRequestWithEmail
                .withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 400" in {
              status(result) shouldBe Status.BAD_REQUEST
            }
          }

          "the email verification service returns PasscodeNotFound" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(PasscodeNotFound))
              TestVerifyPasscodeController.contactPrefSubmit(paperPostRequestWithEmail
                .withFormUrlEncodedBody("passcode" -> "580008"))
            }

            "return 400" in {
              status(result) shouldBe Status.BAD_REQUEST
            }
          }

          "the email verification service returns IncorrectPasscode" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Right(IncorrectPasscode))
              TestVerifyPasscodeController.contactPrefSubmit(paperPostRequestWithEmail
                .withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 400" in {
              status(result) shouldBe Status.BAD_REQUEST
            }
          }

          "the email verification service returns an unexpected error" should {

            lazy val result = {
              mockVerifyPasscodeRequest(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "Err0r")))
              TestVerifyPasscodeController.contactPrefSubmit(paperPostRequestWithEmail
                .withFormUrlEncodedBody("passcode" -> "PASSME"))
            }

            "return 500" in {
              status(result) shouldBe Status.INTERNAL_SERVER_ERROR
            }
          }
        }

        "the form is unsuccessfully submitted" should {

          lazy val result = TestVerifyPasscodeController.contactPrefSubmit(paperPostRequestWithEmail
            .withFormUrlEncodedBody("passcode" -> "FAIL"))

          "return 400" in {
            status(result) shouldBe Status.BAD_REQUEST
          }
        }
      }

      "there is no email in session" when {

        lazy val result = TestVerifyPasscodeController.contactPrefSubmit(postRequestWithPaperPref)

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to the first page in the journey" in {
          redirectLocation(result) shouldBe
            Some(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect.url)
        }
      }
    }

    insolvencyCheck(TestVerifyPasscodeController.contactPrefSubmit())
  }

  "Calling the contactPrefSendVerification action in VerifyPasscodeController" when {

    "the emailPinVerification feature switch is enabled" when {

      "there is an email in session" when {

        "the email is successfully verified" should {

          lazy val result = {
            mockCreateEmailPasscodeRequest(Some(true))
            TestVerifyPasscodeController.contactPrefSendVerification()(paperGetRequestWithEmail)
          }

          "return 303 (SEE_OTHER)" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the contactPrefShow url" in {
            redirectLocation(result) shouldBe
              Some(controllers.email.routes.VerifyPasscodeController.contactPrefShow.url)
          }
        }

        "the email has already been verified" should {

          lazy val result = {
            mockCreateEmailPasscodeRequest(Some(false))
            TestVerifyPasscodeController.contactPrefSendVerification()(paperGetRequestWithEmail)
          }

          "return 303 (SEE_OTHER)" in {
            status(result) shouldBe SEE_OTHER
          }

          "have the correct redirect location" in {
            redirectLocation(result) shouldBe Some(routes.VerifyPasscodeController.updateContactPrefEmail.url)
          }
        }

        "an error comes back from the service" should {

          lazy val result = {
            mockCreateEmailPasscodeRequest(None)
            TestVerifyPasscodeController.contactPrefSendVerification()(paperGetRequestWithEmail)
          }

          "return an INTERNAL_SERVER_ERROR" in {
            status(result) shouldBe INTERNAL_SERVER_ERROR
          }
        }
      }

      "there isn't an email in session" should {

        lazy val result = TestVerifyPasscodeController.contactPrefSendVerification()(getRequestWithPaperPref)

        "return 303 (SEE_OTHER)" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the contact preference redirect route" in {
          redirectLocation(result) shouldBe
            Some(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect.url)
        }
      }
    }

    insolvencyCheck(TestVerifyPasscodeController.contactPrefSendVerification())
  }

  "Calling the updateContactPrefEmail action in VerifyPasscodeController" when {

    "the emailPinVerification feature switch is enabled" when {

      "there is an email in session" when {

        "the email is verified" when {

          "the vat subscription service returns a Right" should {

            lazy val updateEmailMockResponse = Future.successful(Right(UpdateEmailSuccess("success")))

            lazy val result = {
              mockGetEmailVerificationState(testEmail)(Future(Some(true)))
              mockUpdateContactPrefEmailAddress(vrn, testEmail, updateEmailMockResponse)
              TestVerifyPasscodeController.updateContactPrefEmail()(paperPostRequestWithEmail)
            }

            "return SEE_OTHER" in {
              status(result) shouldBe SEE_OTHER
            }

            "redirect to the email change success route" in {
              redirectLocation(result) shouldBe Some(controllers.email.routes.EmailChangeSuccessController.show.url)
            }
          }

          "the vat subscription service returns a conflict error" should {

            lazy val updateEmailMockResponse = Future.successful(Left(ErrorModel(CONFLICT, "")))

            lazy val result = {
              mockGetEmailVerificationState(testEmail)(Future(Some(true)))
              mockUpdateContactPrefEmailAddress(vrn, testEmail, updateEmailMockResponse)
              TestVerifyPasscodeController.updateContactPrefEmail()(paperPostRequestWithEmail)
            }

            "return SEE_OTHER" in {
              status(result) shouldBe SEE_OTHER
            }

            "have the correct redirect location" in {
              redirectLocation(result) shouldBe Some(mockConfig.btaAccountDetailsUrl)
            }
          }

          "the vat subscription service returns an unexpected error" should {

            lazy val updateEmailMockResponse = Future.successful(Left(ErrorModel(1, "")))

            lazy val result = {
              mockGetEmailVerificationState(testEmail)(Future(Some(true)))
              mockUpdateContactPrefEmailAddress(vrn, testEmail, updateEmailMockResponse)
              TestVerifyPasscodeController.updateContactPrefEmail()(paperPostRequestWithEmail)
            }

            "return an internal server error" in {
              status(result) shouldBe INTERNAL_SERVER_ERROR
            }
          }
        }

        "the email isn't verified" should {

          lazy val result = {
            mockGetEmailVerificationState(testEmail)(Future(Some(false)))
            TestVerifyPasscodeController.updateContactPrefEmail()(paperPostRequestWithEmail)
          }

          "return a 303" in {
            status(result) shouldBe SEE_OTHER
          }

          "have the correct redirect location" in {
            redirectLocation(result) shouldBe
              Some(controllers.email.routes.VerifyPasscodeController.contactPrefSendVerification.url)
          }
        }
      }

      "there isn't an email in session" should {

        lazy val result = {
          mockGetEmailVerificationState("")(Future(Some(false)))
          TestVerifyPasscodeController.updateContactPrefEmail()(postRequestWithPaperPref)
        }

        "return 303 (SEE_OTHER)" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the capture email route" in {
          redirectLocation(result) shouldBe
            Some(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect.url)
        }
      }
    }

    insolvencyCheck(TestVerifyPasscodeController.updateContactPrefEmail())
  }

  "Calling the sendUpdateRequest action in VerifyPasscodeController" when {

    "the vat subscription service returns a Right" when {

      "the user has a non-empty email in session" should {

        lazy val updateEmailMockResponse = Future.successful(Right(UpdateEmailSuccess("success")))

        lazy val result = {
          mockGetEmailVerificationState(testEmail)(Future(Some(true)))
          mockUpdateContactPrefEmailAddress(vrn, testEmail, updateEmailMockResponse)
          TestVerifyPasscodeController.sendUpdateRequest(testEmail)(userWithValidationEmail)
        }

        "return a 303" in {
          status(result) shouldBe SEE_OTHER
        }

        "have the correct redirect location" in {
          redirectLocation(result) shouldBe Some(controllers.email.routes.EmailChangeSuccessController.show.url)
        }

        "add the successful email change to session" in {
          session(result).get(SessionKeys.emailChangeSuccessful) shouldBe Some("true")
        }
        "add the inFlightContactDetailsChangeKey to session" in {
          session(result).get(SessionKeys.inFlightContactDetailsChangeKey) shouldBe Some("true")
        }
      }

      "the user has an empty email in session" should {

        lazy val updateEmailMockResponse = Future.successful(Right(UpdateEmailSuccess("success")))

        lazy val result = {
          mockGetEmailVerificationState(testEmail)(Future(Some(true)))
          mockUpdateContactPrefEmailAddress(vrn, testEmail, updateEmailMockResponse)
          TestVerifyPasscodeController.sendUpdateRequest(testEmail)(userWithEmptyValidationEmail)
        }

        "return a 303" in {
          status(result) shouldBe SEE_OTHER
        }

        "have the correct redirect location" in {
          redirectLocation(result) shouldBe Some(controllers.email.routes.EmailChangeSuccessController.show.url)
        }

        "add the successful email change to session" in {
          session(result).get(SessionKeys.emailChangeSuccessful) shouldBe Some("true")
        }
        "add the inFlightContactDetailsChangeKey to session" in {
          session(result).get(SessionKeys.inFlightContactDetailsChangeKey) shouldBe Some("true")
        }
      }

    }

    "the vat subscription service returns a CONFLICT" should {

      lazy val updateEmailMockResponse = Future.successful(Left(ErrorModel(CONFLICT, "")))

      lazy val result = {
        mockGetEmailVerificationState(testEmail)(Future(Some(true)))
        mockUpdateContactPrefEmailAddress(vrn, testEmail, updateEmailMockResponse)
        TestVerifyPasscodeController.sendUpdateRequest(testEmail)(userWithValidationEmail)
      }

      "return a 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "have the correct redirect location" in {
        redirectLocation(result) shouldBe Some(mockConfig.btaAccountDetailsUrl)
      }
    }

    "the vat subscription service returns an error" should {

      lazy val updateEmailMockResponse = Future.successful(Left(ErrorModel(INTERNAL_SERVER_ERROR, "")))

      lazy val result = {
        mockGetEmailVerificationState(testEmail)(Future(Some(true)))
        mockUpdateContactPrefEmailAddress(vrn, testEmail, updateEmailMockResponse)
        TestVerifyPasscodeController.sendUpdateRequest(testEmail)(userWithValidationEmail)
      }

      "return an internal server error" in {
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "not add a successful email change to session" in {
        session(result).get(SessionKeys.emailChangeSuccessful) shouldBe None
      }
      "not add a nFlightContactDetailsChangeKey to session" in {
        session(result).get(SessionKeys.inFlightContactDetailsChangeKey) shouldBe None
      }
    }

  }
}
