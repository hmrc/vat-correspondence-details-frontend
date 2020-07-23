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

package controllers.contactPreference

import common.SessionKeys.inFlightContactDetailsChangeKey
import controllers.ControllerBaseSpec
import controllers.email.routes
import mocks.MockEmailVerificationService
import models.User
import models.customerInformation._
import models.errors.ErrorModel
import play.api.http.Status
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.email.VerifyEmailView

import scala.concurrent.Future

class EmailVerificationControllerSpec extends ControllerBaseSpec with MockEmailVerificationService {

  val controller = new EmailVerificationController(
    mockEmailVerificationService,
    mockErrorHandler,
    mockVatSubscriptionService,
    mockAuditingService,
    inject[VerifyEmailView]
  )

  val emailSessionKey = "vatCorrespondencePrepopulationEmail"
  val email = "some@email.com"
  val vrn = "999999999"

  val ppobAddress = PPOBAddress("", None, None, None, None, None, "")

  def mockCustomer(): Unit = mockGetCustomerInfo(vrn)(Future.successful(Right(CustomerInformation(
    PPOB(ppobAddress, None, None), Some(PendingChanges(Some(PPOB(ppobAddress, None, None)), None)), None, None, None, None, Some("PAPER")
  ))))

  def newUser(sessionVariables: Seq[(String, String)] = Seq()): User[AnyContent] =
    new User[AnyContent](vrn)(FakeRequest().withSession(sessionVariables: _*))

  "Calling .extractSessionEmail" should {

    "pull the users email out of session" when {

      "and email exist in session" in {
        implicit lazy val request: Request[AnyContent] = FakeRequest().withSession(emailSessionKey -> email)
        val user = User[AnyContent](vrn)

        controller.extractSessionEmail(user) shouldBe Some("some@email.com")
      }

    }

    "return a None" when {

      "no email exist in session" in {
        controller.extractSessionEmail(newUser()) shouldBe None
      }

    }

  }

  "Calling .sendUpdateRequest" should {

    "redirect the user to the EmailChangeSuccess page" when {

      "a successful response is returned" which {
        lazy val updateEmailMockResponse = Future.successful(Right(UpdateEmailSuccess("success")))

        lazy val result = {
          mockUpdateContactPrefEmailAddress(vrn, email, updateEmailMockResponse)
          controller.sendUpdateRequest(email)(newUser())
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
          mockUpdateContactPrefEmailAddress(vrn, email, updateEmailMockResponse)
          controller.sendUpdateRequest(email)(newUser())
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
          mockUpdateContactPrefEmailAddress(vrn, email, updateEmailMockResponse)
          controller.sendUpdateRequest(email)(newUser())
        }

        s"has a status of $SEE_OTHER" in {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "Calling .updateContactPrefEmail" should {

    "redirect to the email successful page" when {

      "the user has a verified email" which {
        lazy val updateEmailMockResponse = Future.successful(Right(UpdateEmailSuccess("success")))

        lazy val user = newUser(Seq(emailSessionKey -> email))

        lazy val result = {
          mockCustomer()
          mockGetEmailVerificationState(email)(Future.successful(Some(true)))
          mockUpdateContactPrefEmailAddress(vrn, email, updateEmailMockResponse)
          controller.updateContactPrefEmail()(user)
        }

        s"has a status of $SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the correct page" in {
          redirectLocation(result) shouldBe Some(controllers.email.routes.EmailChangeSuccessController.show().url)
        }
      }
    }

    "redirect to the check verification status route" when {

      "the user has an unverified email" which {
        lazy val user = newUser(Seq(emailSessionKey -> email))

        lazy val result = {
          mockCustomer()
          mockGetEmailVerificationState(email)(Future.successful(Some(false)))
          controller.updateContactPrefEmail()(user)
        }

        s"has a status of $SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the correct route" in {
          redirectLocation(result) shouldBe Some(routes.EmailVerificationController.checkVerificationStatus().url)
        }
      }
    }

    "redirect to the contact preference redirect route" when {

      "the user does not have an email in session" which {
        lazy val result = {
          mockCustomer()
          controller.updateContactPrefEmail()(newUser())
        }

        s"has a status of $SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the correct page" in {
          redirectLocation(result) shouldBe Some(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect().url)
        }
      }
    }
  }

  "Calling .checkVerificationStatus" should {

    "redirect to the updateContactPrefEmail route" when {

      "the email in session is verified" which {
        lazy val result = {
          mockCustomer()
          mockGetEmailVerificationState(email)(Future.successful(Some(true)))
          controller.checkVerificationStatus()(newUser(Seq(emailSessionKey -> email)))
        }

        s"has a status of $SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the correct route" in {
          redirectLocation(result) shouldBe Some(routes.EmailVerificationController.updateContactPrefEmail().url)
        }
      }

      "the getVerifiedStatus returns false, but the createVerifiedRequest claims it is verified" which {
        lazy val result = {
          mockCustomer()
          mockGetEmailVerificationState(email)(Future.successful(Some(false)))
          mockCreateEmailVerificationRequest(Some(false))
          controller.checkVerificationStatus()(newUser(Seq(emailSessionKey -> email)))
        }

        s"has a status of $SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the correct route" in {
          redirectLocation(result) shouldBe Some(routes.EmailVerificationController.updateContactPrefEmail().url)
        }
      }
    }

    "redirect to the verify email page" when {

      "the users email is not verified, but a request to verify successfully sends" which {
        lazy val result = {
          mockCustomer()
          mockGetEmailVerificationState(email)(Future.successful(Some(false)))
          mockCreateEmailVerificationRequest(Some(true))
          controller.checkVerificationStatus()(newUser(Seq(emailSessionKey -> email)))
        }

        s"has a status of $SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the correct route" in {
          redirectLocation(result) shouldBe Some(routes.EmailVerificationController.show().url)
        }
      }
    }

    "return an internal server error" when {

      "the users email is not verified, and the request to verify returns nothing" in {
        lazy val result = {
          mockCustomer()
          mockGetEmailVerificationState(email)(Future.successful(Some(false)))
          mockCreateEmailVerificationRequest(None)
          controller.checkVerificationStatus()(newUser(Seq(emailSessionKey -> email)))
        }

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "redirect to the contact preference redirect route" when {

      "the users email is not verified, but a request to verify successfully sends" which {
        lazy val result = {
          mockCustomer()
          controller.checkVerificationStatus()(newUser())
        }

        s"has a status of $SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirects to the correct route" in {
          redirectLocation(result) shouldBe Some(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect().url)
        }
      }
    }
  }

  "Calling the show action in VerifyEmailController" when {

    "there is an email in session" should {

      lazy val result = {
        mockIndividualAuthorised()
        controller.show()(requestWithEmail)
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
        controller.show()(newUser(Seq(inFlightContactDetailsChangeKey -> "false")))
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
        controller.show()(requestWithEmail)
      }

      "return forbidden (403)" in {
        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }
}
