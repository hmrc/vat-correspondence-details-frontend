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

import assets.BaseTestConstants.vrn
import assets.CustomerInfoConstants._
import common.SessionKeys
import controllers.ControllerBaseSpec
import mocks.MockEmailVerificationService
import models.User
import models.customerInformation._
import models.errors.ErrorModel
import play.api.http.{HeaderNames, Status}
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class VerifyEmailControllerSpec extends ControllerBaseSpec with MockEmailVerificationService {

  object TestVerifyEmailController extends VerifyEmailController(
    mockEmailVerificationService,
    mockErrorHandler,
    mockVatSubscriptionService
  )

  lazy val emptyEmailSessionRequest: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(SessionKeys.prepopulationEmailKey -> "")

  val ppobAddress: PPOBAddress = PPOBAddress("", None, None, None, None, None, "")
  val currentEmail = "current@email.com"

  def mockCustomer(): Unit = mockGetCustomerInfo(vrn)(Future.successful(Right(CustomerInformation(
    PPOB(ppobAddress, None, None),
    Some(PendingChanges(Some(PPOB(ppobAddress, None, None)), None)),
    None,
    None,
    None,
    None,
    Some("PAPER"),
    isInsolvent = false,
    Some(true),
    None
  ))))


  "Calling the extractSessionEmail function in VerifyEmailController" when {

    "there is an authenticated request from a user with an email in session" should {

      "result in an email address being retrieved if there is an email" in {
        val userWithSession = User[AnyContent](vrn)(requestWithEmail)
        TestVerifyEmailController.extractSessionEmail(userWithSession) shouldBe Some(testEmail)
      }
    }
  }

  "Calling .btaVerifyEmailRedirect" when {

    "the user has come from BTA" when {

      "the user has an unverified email " should {

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

        "redirects to the send-passcode URL" in {
          redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/send-passcode")
        }

        "add the session keys" in {
          session(result).get(SessionKeys.prepopulationEmailKey) shouldBe Some("pepsimac@gmail.com")
          session(result).get(SessionKeys.inFlightContactDetailsChangeKey) shouldBe Some("false")
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

        "redirects to BTA account details" in {
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

        "redirects to the change email address page" in {
          redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/change-email-address")
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
            redirectLocation(result) shouldBe Some(routes.VerifyPasscodeController.emailSendVerification().url)
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

      insolvencyCheck(TestVerifyEmailController.btaVerifyEmailRedirect())
    }
  }
}
