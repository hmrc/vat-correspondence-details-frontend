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

package pages.email

import assets.BaseITConstants.internalServerErrorTitle
import common.SessionKeys
import helpers.SessionCookieCrumbler
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse
import stubs.{EmailVerificationStub, VatSubscriptionStub}

class ConfirmEmailPageSpec extends BasePageISpec {

  val confirmEmailPath = "/confirm-email-address"
  val updateEmailPath = "/update-email-address"
  val email = "test@test.com"

  "Calling the Capture email (.show) route" when {

    "the user is authenticated" when {

      "there is an email in session" should {

        def show: WSResponse = get(confirmEmailPath, formatEmail(Some(email)) ++ formatInflightPPOB(Some("false")))

        "render the confirm email page" in {

          given.user.isAuthenticated

          And("a successful response for an individual is stubbed")
          VatSubscriptionStub.stubCustomerInfo

          When("the Confirm email page is called")
          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(messages("confirmEmail.title"))
          )
        }
      }

      "there is not an email in session" should {

        "render the capture email page" in {

          def show: WSResponse = get(confirmEmailPath, formatInflightPPOB(Some("false")))

          given.user.isAuthenticated

          When("the Capture email page is called")
          val result = show

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(controllers.email.routes.CaptureEmailController.show().url)
          )
        }
      }

      "an error response is received for Customer Details" should {

        def show: WSResponse = get(confirmEmailPath, formatEmail(Some(email)))

        "render the technical difficulties page" in {

          given.user.isAuthenticated

          And("an error response for an individual is stubbed")
          VatSubscriptionStub.stubCustomerInfoError

          When("the Capture email page is called")
          val result = show

          result should have(
            httpStatus(Status.INTERNAL_SERVER_ERROR),
            pageTitle(internalServerErrorTitle)
          )
        }
      }
    }

    "the user is an authenticated Agent" should {

      def show: WSResponse = get(confirmEmailPath, formatEmail(Some(email)))

      "render the not authorised page" in {

        given.user.isAuthenticatedAgent

        When("the Confirm email page is called")
        val result = show

        result should have(
          httpStatus(Status.UNAUTHORIZED),
          pageTitle("You cannot change your client’s email address yet")
        )
      }
    }

    "the user is not enrolled for MTD VAT" should {

      def show: WSResponse = get(confirmEmailPath, formatEmail(Some(email)))

      "render the not authorised page" in {

        given.user.isNotEnrolled

        When("the Confirm email page is called")
        val result = show

        result should have(
          httpStatus(Status.FORBIDDEN),
          pageTitle("You can not use this service yet")
        )
      }
    }
  }

  "Calling the update email address route" when {

    "the user is authenticated" when {

      "there is an email in session" when {

        def show: WSResponse = get(
          updateEmailPath,
          formatEmail(Some(email)) ++ formatValidationEmail(Some(email)) ++ formatInflightPPOB(Some("false"))
        )

        "the vat subscription service successfully updates the email" should {

          "redirect to the Email Change Success Controller" in {

            given.user.isAuthenticated

            When("The update email address route is called")

            And("a successful email update response is stubbed")
            EmailVerificationStub.stubEmailVerified(email)

            And("a successful customer information response is stubbed")
            VatSubscriptionStub.stubCustomerInfo

            And("a successful vat subscription response is stubbed")
            VatSubscriptionStub.stubUpdatePPOB
            val result = show

            result should have(
              httpStatus(Status.SEE_OTHER),
              redirectURI(controllers.email.routes.EmailChangeSuccessController.show().url)
            )
          }

          "remove the email, validationEmail and inflightPPOBKey from session" in {

            given.user.isAuthenticated

            When("The update email address route is called")

            And("a successful email update response is stubbed")
            EmailVerificationStub.stubEmailVerified(email)

            And("a successful customer information response is stubbed")
            VatSubscriptionStub.stubCustomerInfo

            And("a successful vat subscription response is stubbed")
            VatSubscriptionStub.stubUpdatePPOB
            val result = show

            SessionCookieCrumbler.getSessionMap(result).get(SessionKeys.prepopulationEmailKey) shouldBe None
            SessionCookieCrumbler.getSessionMap(result).get(SessionKeys.validationEmailKey) shouldBe None
            SessionCookieCrumbler.getSessionMap(result).get(SessionKeys.inFlightContactDetailsChangeKey) shouldBe None
          }
        }

        "the email verification service says the email is not verified" should {

          "redirect to the send verification controller" in {

            given.user.isAuthenticated

            When("The update email address route is called")

            And("a successful email update response is stubbed")
            EmailVerificationStub.stubEmailNotVerified

            And("a successful customer information response is stubbed")
            VatSubscriptionStub.stubCustomerInfo

            And("a successful vat subscription response is stubbed")
            VatSubscriptionStub.stubUpdatePPOBNoMessage
            val result = show

            result should have(
              httpStatus(Status.SEE_OTHER),
              redirectURI(controllers.email.routes.VerifyEmailController.sendVerification().url)
            )
          }
        }

        "the email verification service returns an error" should {

          "render the internal error page" in {

            given.user.isAuthenticated

            When("The update email address route is called")

            And("a successful customer information response is stubbed")
            VatSubscriptionStub.stubCustomerInfo

            And("an email verification check error response is stubbed")
            EmailVerificationStub.stubEmailVerifiedError
            val result = show

            result should have(
              httpStatus(Status.INTERNAL_SERVER_ERROR),
              pageTitle(internalServerErrorTitle)
            )
          }
        }
      }

      "there is not an email in session" should {

        def show: WSResponse = get(updateEmailPath, formatInflightPPOB(Some("false")))

        "redirect to the Capture Email controller" in {

          given.user.isAuthenticated

          When("The update email address route is called")

          And("a successful email update response is stubbed")
          EmailVerificationStub.stubEmailVerified(email)

          And("a successful vat subscription response is stubbed")
          VatSubscriptionStub.stubUpdatePPOB
          val result = show

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(controllers.email.routes.CaptureEmailController.show().url)
          )
        }
      }
    }

    "the user is an authenticated Agent" should {

      def show: WSResponse = get(updateEmailPath, formatEmail(Some(email)))

      "render the not authorised page" in {

        given.user.isAuthenticatedAgent

        When("the Confirm email page is called")
        val result = show

        result should have(
          httpStatus(Status.UNAUTHORIZED),
          pageTitle("You cannot change your client’s email address yet")
        )
      }
    }

    "the user is not enrolled for MTD VAT" should {

      def show: WSResponse = get(updateEmailPath, formatEmail(Some(email)))

      "render the not authorised page" in {

        given.user.isNotEnrolled

        When("the Confirm email page is called")
        val result = show

        result should have(
          httpStatus(Status.FORBIDDEN),
          pageTitle("You can not use this service yet")
        )
      }
    }
  }
}
