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

package pages

import common.SessionKeys
import helpers.SessionCookieCrumbler
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

        def show: WSResponse = get(confirmEmailPath, formatEmail(Some(email)))

        "render the confirm email page" in {

          given.user.isAuthenticated

          And("a successful response for an individual is stubbed")
          VatSubscriptionStub.stubCustomerInfo

          When("the Confirm email page is called")
          val result = show

          result should have {
            httpStatus(Status.OK)
            pageTitle(messages("confirmEmail.title"))
          }
        }
      }

      "there is not an email in session" should {

        "render the confirm email page" in {

          def show: WSResponse = get(confirmEmailPath)

          given.user.isAuthenticated

          And("a successful response for an individual is stubbed")
          VatSubscriptionStub.stubCustomerInfo

          When("the Capture email page is called")
          val result = show

          result should have {
            httpStatus(Status.SEE_OTHER)
            redirectURI(controllers.routes.CaptureEmailController.show().url)
          }
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

          result should have {
            httpStatus(Status.OK)
            pageTitle("Sorry, we are experiencing technical difficulties - 500")
          }
        }
      }
    }

    "the user is not authenticated" should {

      def show: WSResponse = get(confirmEmailPath, formatEmail(Some(email)))

      "render the not authorised page" in {

        given.user.isNotEnrolled

        When("the Confirm email page is called")
        val result = show

        result should have {
          httpStatus(Status.OK)
          pageTitle("You cannot change your client’s correspondence details yet")
        }
      }
    }
  }

  "Calling the Capture email (.updateEmailAddress) route" when {

    "the user is authenticated" when {

      "there is an email in session" when {

        def show: WSResponse = get(updateEmailPath, formatEmail(Some(email)))

        "the vat subscription service successfully updates the email" should {

          "redirect to the Email Change Success Controller" in {

            given.user.isAuthenticated

            When("The update email address route is called")

            And("a successful email update response is stubbed")
            EmailVerificationStub.stubEmailVerified

            And("a successful customer information response is stubbed")
            VatSubscriptionStub.stubCustomerInfo

            And("a successful vat subscription response is stubbed")
            VatSubscriptionStub.stubUpdateEmail
            val result = show

            result should have {
              httpStatus(Status.SEE_OTHER)
              redirectURI(controllers.routes.EmailChangeSuccessController.show().url)
            }
          }

          "remove the email, validationEmail and inflightPPOBKey from session" in {

            def show: WSResponse = get(updateEmailPath, formatEmail(Some(email)))

            given.user.isAuthenticated

            When("The update email address route is called")

            And("a successful email update response is stubbed")
            EmailVerificationStub.stubEmailVerified

            And("a successful customer information response is stubbed")
            VatSubscriptionStub.stubCustomerInfo

            And("a successful vat subscription response is stubbed")
            VatSubscriptionStub.stubUpdateEmail
            val result = show

            SessionCookieCrumbler.getSessionMap(result).get(SessionKeys.emailKey) shouldBe None
            SessionCookieCrumbler.getSessionMap(result).get(SessionKeys.validationEmailKey) shouldBe None

            //TODO Make sure this removed from session
//            SessionCookieCrumbler.getSessionMap(result).get(SessionKeys.inflightPPOBKey) shouldBe None
          }
        }

        "the vat subscription service returns is says the email is not verified" should {

          "redirect to the send verification controller" in {

            given.user.isAuthenticated

            When("The update email address route is called")

            And("a successful email update response is stubbed")
            EmailVerificationStub.stubEmailNotVerified

            And("a successful customer information response is stubbed")
            VatSubscriptionStub.stubCustomerInfo

            And("a successful vat subscription response is stubbed")
            VatSubscriptionStub.stubUpdateEmailNoMessage
            val result = show

            result should have {
              httpStatus(Status.SEE_OTHER)
              redirectURI(controllers.routes.VerifyEmailController.sendVerification().url)
            }
          }
        }

        "the email verification service returns an error" should {

          "render the internal error page" in {

            given.user.isAuthenticated

            When("The update email address route is called")

            And("a successful email update response is stubbed")
            EmailVerificationStub.stubVerificationRequestError

            And("a successful customer information response is stubbed")
            VatSubscriptionStub.stubCustomerInfo

            And("a successful vat subscription response is stubbed")
            VatSubscriptionStub.stubUpdateEmailError
            val result = show

            result should have {
              httpStatus(Status.OK)
              pageTitle("Sorry, we are experiencing technical difficulties - 500")
            }

          }

        }
      }

      "there is not an email in session" should {

        def show: WSResponse = get(updateEmailPath)

        "redirect to the Capture Email controller" in {

          given.user.isAuthenticated

          When("The update email address route is called")

          And("a successful email update response is stubbed")
          EmailVerificationStub.stubEmailVerified

          And("a successful customer information response is stubbed")
          VatSubscriptionStub.stubCustomerInfo

          And("a successful vat subscription response is stubbed")
          VatSubscriptionStub.stubUpdateEmail
          val result = show

          result should have {
            httpStatus(Status.SEE_OTHER)
            redirectURI(controllers.routes.CaptureEmailController.show().url)
          }
        }
      }
    }

    "the is not authenticated" should {

      def show: WSResponse = get(updateEmailPath, formatEmail(Some(email)))

      "render the not authorised page" in {

        given.user.isNotEnrolled

        When("the Confirm email page is called")
        val result = show

        result should have {
          httpStatus(Status.OK)
          pageTitle("You cannot change your client’s correspondence details yet")
        }
      }

    }
  }
}
