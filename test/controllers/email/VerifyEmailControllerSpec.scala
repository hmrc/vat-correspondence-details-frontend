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

import assets.CustomerInfoConstants._
import common.SessionKeys
import controllers.ControllerBaseSpec
import mocks.MockEmailVerificationService
import models.User
import models.customerInformation._
import play.api.http.{HeaderNames, Status}
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.email.VerifyEmailView
import assets.BaseTestConstants.vrn
import models.errors.ErrorModel

import scala.concurrent.Future
import models.contactPreferences.ContactPreference._

class VerifyEmailControllerSpec extends ControllerBaseSpec with MockEmailVerificationService {

  object TestVerifyEmailController extends VerifyEmailController(

    mockEmailVerificationService,
    mockErrorHandler,
    mockVatSubscriptionService,
    injector.instanceOf[VerifyEmailView]
  )

  lazy val emptyEmailSessionRequest: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(SessionKeys.prepopulationEmailKey -> "")

  val ppobAddress = PPOBAddress("", None, None, None, None, None, "")

  def mockCustomer(): Unit = mockGetCustomerInfo(vrn)(Future.successful(Right(CustomerInformation(
    PPOB(ppobAddress, None, None), Some(PendingChanges(Some(PPOB(ppobAddress, None, None)), None)), None, None, None, None, Some("PAPER")
  ))))


  "Calling the extractSessionEmail function in VerifyEmailController" when {

    "there is an authenticated request from a user with an email in session" should {

      "result in an email address being retrieved if there is an email" in {
        val userWithSession = User[AnyContent](vrn)(requestWithEmail)
        TestVerifyEmailController.extractSessionEmail(userWithSession) shouldBe Some(testEmail)
      }
    }
  }

  "Calling the emailShow action in VerifyEmailController" when {

    "there is an email in session" should {

      lazy val result = {
        TestVerifyEmailController.emailShow()(requestWithEmail)
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
        TestVerifyEmailController.emailShow()(emptyEmailSessionRequest)
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
        mockIndividualWithoutEnrolment()
        TestVerifyEmailController.emailShow()(requestWithEmail)
      }

      "return forbidden (403)" in {
        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }

  "Calling the emailSendVerification action in VerifyEmailController" when {

    "there is an email in session and the email request is successfully created" should {

      lazy val result = {
        mockCreateEmailVerificationRequest(Some(true))
        TestVerifyEmailController.emailSendVerification()(requestWithEmail)
      }

      "return 303 (SEE_OTHER)" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect to the verify your email page" in {
        redirectLocation(result) shouldBe Some(routes.VerifyEmailController.emailShow().url)
      }
    }

    "there is an email in session and the email request is not created as already verified" should {

      lazy val result = {
        mockCreateEmailVerificationRequest(Some(false))
        TestVerifyEmailController.emailSendVerification()(requestWithEmail)
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
        mockCreateEmailVerificationRequest(None)
        TestVerifyEmailController.emailSendVerification()(requestWithEmail)
      }

      "return 500 (INTERNAL_SERVER_ERROR)" in {
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "there isn't an email in session" should {

      lazy val result = {
        TestVerifyEmailController.emailSendVerification()(emptyEmailSessionRequest)
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
        mockIndividualWithoutEnrolment()
        TestVerifyEmailController.emailSendVerification()(requestWithEmail)
      }

      "return forbidden (403)" in {
        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }

  "Calling the contactPrefShow action in VerifyEmailController" when {

    "there is an email in session" should {

      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(true)
        TestVerifyEmailController.contactPrefShow()(requestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper))
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
        mockConfig.features.letterToConfirmedEmailEnabled(true)
        TestVerifyEmailController.contactPrefShow()(emptyEmailSessionRequest.withSession(SessionKeys.currentContactPrefKey -> paper))
      }

      "return 303 (SEE_OTHER)" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect to the start of the contact preference journey" in {
        redirectLocation(result) shouldBe Some(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect().url)
      }
    }

    "the user is not authorised" should {

      lazy val result = {
        mockIndividualWithoutEnrolment()
        TestVerifyEmailController.contactPrefShow()(requestWithEmail)
      }

      "return forbidden (403)" in {
        status(result) shouldBe Status.FORBIDDEN
      }
    }

    "the letterToConfirmedEmailEnabled feature switch is off" should {

      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(false)
        TestVerifyEmailController.contactPrefShow()(requestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper))
      }

      "return page not found (404)" in {
        status(result) shouldBe Status.NOT_FOUND
      }
    }
  }

  "Calling the contactPrefSendVerification action in VerifyEmailController" when {

    "the email in session is verified" should {

      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(true)
        mockGetEmailVerificationState(testEmail)(Future.successful(Some(true)))
        TestVerifyEmailController.contactPrefSendVerification()(requestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper))
      }

      "return 303 (SEE_OTHER)" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect to the correct route" in {
        redirectLocation(result) shouldBe Some(routes.VerifyEmailController.updateContactPrefEmail().url)
      }
    }

    "the getVerifiedStatus returns false, but the createVerifiedRequest claims it is verified" which {
      lazy val result = {
        mockGetEmailVerificationState(testEmail)(Future.successful(Some(false)))
        mockCreateEmailVerificationRequest(Some(false))
        TestVerifyEmailController.contactPrefSendVerification()(requestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper))
      }

      s"has a status of $SEE_OTHER" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirects to the correct route" in {
        redirectLocation(result) shouldBe Some(routes.VerifyEmailController.updateContactPrefEmail().url)
      }
    }

    "the users email is not verified, but a request to verify successfully sends" should {

      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(true)
        mockGetEmailVerificationState(testEmail)(Future.successful(Some(false)))
        mockCreateEmailVerificationRequest(Some(true))
        TestVerifyEmailController.contactPrefSendVerification()(requestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper))
      }

      "return 303 (SEE_OTHER)" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect to the verify email page" in {
        redirectLocation(result) shouldBe Some(routes.VerifyEmailController.contactPrefShow().url)
      }
    }

    "the users email is not verified, and the request to verify returns nothing" should {

      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(true)
        mockGetEmailVerificationState(testEmail)(Future.successful(Some(false)))
        mockCreateEmailVerificationRequest(None)
        TestVerifyEmailController.contactPrefSendVerification()(requestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper))
      }

      "return 500 (INTERNAL_SERVER_ERROR)" in {
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "there isn't an email in session" should {

      lazy val result = {
        TestVerifyEmailController.contactPrefSendVerification()(emptyEmailSessionRequest.withSession(SessionKeys.currentContactPrefKey -> paper))
      }

      "return 303 (SEE_OTHER)" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect to the contact preference redirect route" in {
        redirectLocation(result) shouldBe Some(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect().url)
      }
    }

    "the user is not authorised" should {

      lazy val result = {
        mockIndividualWithoutEnrolment()
        TestVerifyEmailController.contactPrefSendVerification()(requestWithEmail)
      }

      "return forbidden (403)" in {
        status(result) shouldBe Status.FORBIDDEN
      }
    }

    "the letterToConfirmedEmailEnabled feature switch is off" should {

      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(false)
        TestVerifyEmailController.contactPrefSendVerification()(requestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper))
      }

      "return page not found (404)" in {
        status(result) shouldBe Status.NOT_FOUND
      }
    }
  }

  "Calling .updateContactPrefEmail" should {

    "redirect to the email successful page" when {

      "the user has a verified email" which {
        lazy val updateEmailMockResponse = Future.successful(Right(UpdateEmailSuccess("success")))

        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          mockCustomer()
          mockGetEmailVerificationState(testEmail)(Future.successful(Some(true)))
          mockUpdateContactPrefEmailAddress(vrn, testEmail, updateEmailMockResponse)
          TestVerifyEmailController.updateContactPrefEmail()(requestWithEmail)
        }

        s"has a status of $SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the correct page" in {
          redirectLocation(result) shouldBe Some(controllers.email.routes.EmailChangeSuccessController.show().url)
        }

        "add emailChangeSuccessful to session" in {
          session(result).get(SessionKeys.emailChangeSuccessful) shouldBe Some("true")
        }
      }
    }

    "redirect to the check verification status route" when {

      "the user has an unverified email" which {

        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          mockCustomer()
          mockGetEmailVerificationState(testEmail)(Future.successful(Some(false)))
          TestVerifyEmailController.updateContactPrefEmail()(requestWithEmail)
        }

        s"has a status of $SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the correct route" in {
          redirectLocation(result) shouldBe Some(routes.VerifyEmailController.contactPrefSendVerification().url)
        }
      }
    }

    "redirect to the contact preference redirect route" when {

      "the user does not have an email in session" which {
        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          mockCustomer()
          TestVerifyEmailController.updateContactPrefEmail()(request)
        }

        s"has a status of $SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the correct page" in {
          redirectLocation(result) shouldBe Some(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect().url)
        }
      }
    }

    "the letterToConfirmedEmailEnabled feature switch is off" should {

      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(false)
        TestVerifyEmailController.contactPrefSendVerification()(requestWithEmail.withSession(SessionKeys.currentContactPrefKey -> paper))
      }

      "return page not found (404)" in {
        status(result) shouldBe Status.NOT_FOUND
      }
    }
  }

  "Calling .sendUpdateRequest" should {

    "redirect the user to the EmailChangeSuccess page" when {

      "a successful response is returned" which {
        lazy val updateEmailMockResponse = Future.successful(Right(UpdateEmailSuccess("success")))

        lazy val result = {
          mockUpdateContactPrefEmailAddress(vrn, testEmail, updateEmailMockResponse)
          TestVerifyEmailController.sendUpdateRequest(testEmail)(new User[AnyContent](vrn))
        }

        s"has a status of $SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the correct URL" in {
          redirectLocation(result) shouldBe Some(controllers.email.routes.EmailChangeSuccessController.show().url)
        }
      }
    }

    "redirect the user to the vat subscription service" when {

      "a conflict error response is returned" which {
        lazy val updateEmailMockResponse = Future.successful(Left(ErrorModel(CONFLICT, "NO")))

        lazy val result = {
          mockUpdateContactPrefEmailAddress(vrn, testEmail, updateEmailMockResponse)
          TestVerifyEmailController.sendUpdateRequest(testEmail)(new User[AnyContent](vrn))
        }

        s"has a status of $SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the correct URL" in {
          redirectLocation(result) shouldBe Some("/bta-account-details")
        }
      }
    }

    "show an internal server error" when {

      "a non-conflict error is returned" which {
        lazy val updateEmailMockResponse = Future.successful(Left(ErrorModel(INTERNAL_SERVER_ERROR, "NO")))

        lazy val result = {
          mockUpdateContactPrefEmailAddress(vrn, testEmail, updateEmailMockResponse)
          TestVerifyEmailController.sendUpdateRequest(testEmail)(new User[AnyContent](vrn))
        }

        s"has a status of $SEE_OTHER" in {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "Calling .btaVerifyEmailRedirect" when {

    "the user has come from BTA" when {

      "the user has an unverified email" should {

        lazy val result = {
          mockGetCustomerInfo(vrn)(Future.successful(Right(fullCustomerInfoModel.copy(
            pendingChanges = None,
            ppob = fullPPOBModel.copy(
              contactDetails = Some(contactDetailsUnverifiedEmail)
            )
          ))))
          TestVerifyEmailController.btaVerifyEmailRedirect()(emptyEmailSessionRequest
            .withHeaders(HeaderNames.REFERER -> mockConfig.btaAccountDetailsUrl)
          )
        }

        s"has a status of $SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the send-verification URL" in {
          redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/send-verification")
        }

        "add the users email to session" in {
          session(result).get(SessionKeys.prepopulationEmailKey) shouldBe Some("pepsimac@gmail.com")
        }
      }

      "the user has a verified email" should {
        lazy val result = {
          mockGetCustomerInfo(vrn)(Future.successful(Right(fullCustomerInfoModel.copy(
            pendingChanges = None
          ))))
          TestVerifyEmailController.btaVerifyEmailRedirect()(emptyEmailSessionRequest
            .withHeaders(HeaderNames.REFERER -> mockConfig.btaAccountDetailsUrl)
          )
        }

        s"has a status of $SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the send-verification URL" in {
          redirectLocation(result) shouldBe Some("/bta-account-details")
        }
      }

      "the user has no email" should {
        lazy val result = {
          mockCustomer()
          TestVerifyEmailController.btaVerifyEmailRedirect()(emptyEmailSessionRequest
            .withHeaders(HeaderNames.REFERER -> mockConfig.btaAccountDetailsUrl)
          )
        }

        s"has a status of $SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the send-verification URL" in {
          redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/change-email-address")
        }
      }

      "getCustomerInfo returns an error" should {
        lazy val result = {
          mockGetCustomerInfo(vrn)(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "error")))
          TestVerifyEmailController.btaVerifyEmailRedirect()(emptyEmailSessionRequest
            .withHeaders(HeaderNames.REFERER -> mockConfig.btaAccountDetailsUrl)
          )
        }

        s"has a status of $INTERNAL_SERVER_ERROR" in {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "the user has not come from BTA" should {
      lazy val result = TestVerifyEmailController.btaVerifyEmailRedirect()(emptyEmailSessionRequest)

      "throw an ISE" in {
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

  }
}
