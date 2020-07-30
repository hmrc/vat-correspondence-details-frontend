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

package pages.email

import assets.BaseITConstants.internalServerErrorTitle
import models.contactPreferences.ContactPreference.paper
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse
import stubs.{EmailVerificationStub, VatSubscriptionStub}

class VerifyEmailPageSpec extends BasePageISpec {

  val verifyEmailPath = "/verify-email-address"
  val sendVerificationPath = "/send-verification"
  val verifyContactPrefPath = "/contact-preference/verify-email-address"
  val contactPrefSendVerificationPath = "/contact-preference/send-verification"
  val email = "test@test.com"

  "Calling the Verify Email Address route" when {

    "the user is authenticated" when {

      "there is an email in session" should {

        def show: WSResponse = get(verifyEmailPath, formatEmail(Some(email)) ++ formatInflightChange(Some("false")))

        "render the verify email view" in {

            given.user.isAuthenticated

            And("a successful response for an individual is stubbed")
            VatSubscriptionStub.stubCustomerInfo

            When("the Verify email page is called")
            val result = show

            result should have(
              httpStatus(Status.OK),
              pageTitle(generateDocumentTitle("verifyEmail.title"))
            )
          }
        }
      }

      "there is not an email in session" should {

        def show: WSResponse = get(verifyEmailPath, formatInflightChange(Some("false")))

        "redirect to the Capture Email Address controller" in {

          given.user.isAuthenticated

          When("the Verify email page is called")
          val result = show

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(controllers.email.routes.CaptureEmailController.show().url)
          )
        }
      }

    "the user is an authenticated agent" should {

      def show: WSResponse = get(verifyEmailPath, formatEmail(Some(email)))

      "render the Agent unauthorised view" in {

        given.user.isAuthenticatedAgent

        When("the Verify email page is called")
        val result = show

        result should have(
          httpStatus(Status.UNAUTHORIZED),
          elementText("h1")("You cannot change your client’s email address yet")
        )
      }
    }

    "the user is not enrolled for MTD VAT" should {

      def show: WSResponse = get(verifyEmailPath, formatEmail(Some(email)))

      "render the unauthorised view" in {

        given.user.isNotEnrolled

        When("the Verify email page is called")
        val result = show

        result should have(
          httpStatus(Status.FORBIDDEN),
          pageTitle(generateDocumentTitle("You can not use this service yet", isAgent = None))
        )
      }
    }
  }

  "Calling the Send Verification route" when {

    "the user is authenticated" when {

      "there is an email in session" should {

        "redirect to the verify email page" in {

          def show: WSResponse = get(sendVerificationPath, formatEmail(Some(email)) ++ formatInflightChange(Some("false")))

          given.user.isAuthenticated

          And("a successful response from the email verification service is stubbed")
          EmailVerificationStub.stubVerificationRequestSent

          When("The verify email page is called")
          val result = show

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(controllers.email.routes.VerifyEmailController.emailShow().url)
          )
        }

        "return a false from the Email Verification service" should {

          def show: WSResponse = get(sendVerificationPath, formatEmail(Some(email)) ++ formatInflightChange(Some("false")))

          "redirect to the Confirm Email controller" in {

            given.user.isAuthenticated

            And("a conflict response from the email verification service is stubbed")
            EmailVerificationStub.stubEmailAlreadyVerified

            When("The verify email page is called")
            val result = show

            result should have(
              httpStatus(Status.SEE_OTHER),
              redirectURI(controllers.email.routes.ConfirmEmailController.updateEmailAddress().url)
            )
          }
        }

        "return None from the Email Verification service" should {

          def show: WSResponse = get(sendVerificationPath, formatEmail(Some(email)) ++ formatInflightChange(Some("false")))

          "render the internal server error page" in {

            given.user.isAuthenticated

            And("an error from the email verification service is stubbed")
            EmailVerificationStub.stubVerificationRequestError

            When("the verify email page is called")
            val result = show

            result should have(
              httpStatus(Status.INTERNAL_SERVER_ERROR),
              pageTitle(generateDocumentTitle(internalServerErrorTitle))
            )
          }
        }
      }

      "there is not an email in session" should {

        def show: WSResponse = get(sendVerificationPath, formatInflightChange(Some("false")))

        "redirect to the Capture Email Address controller" in {

          given.user.isAuthenticated

          When("the Verify email page is called")
          val result = show

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(controllers.email.routes.CaptureEmailController.show().url)
          )
        }
      }
    }

    "the user is an authenticated Agent" should {

      def show: WSResponse = get(sendVerificationPath, formatEmail(Some(email)))

      "render the unauthorised view" in {

        given.user.isAuthenticatedAgent

        When("the Verify email page is called")
        val result = show

        result should have(
          httpStatus(Status.UNAUTHORIZED),
          elementText("h1")("You cannot change your client’s email address yet")
        )
      }
    }

    "the user is not enrolled for MTD VAT" should {

      def show: WSResponse = get(sendVerificationPath, formatEmail(Some(email)))

      "render the unauthorised view" in {

        given.user.isNotEnrolled

        When("the Verify email page is called")
        val result = show

        result should have(
          httpStatus(Status.FORBIDDEN),
          pageTitle(generateDocumentTitle("You can not use this service yet", isAgent = None))
        )
      }
    }
  }

  "Calling the Contact Preference Verify Email Address route" when {

    "the user is authenticated" when {

      "there is an email in session" should {

        def show: WSResponse = get(verifyContactPrefPath,
          formatEmail(Some(email)) ++ formatCurrentContactPref(Some(paper)) ++ formatInflightChange(Some("false"))
        )

        "render the verify email view" in {

          given.user.isAuthenticated

          And("a successful response for an individual is stubbed")
          VatSubscriptionStub.stubCustomerInfo

          When("the Verify email page is called")
          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(generateDocumentTitle("verifyEmail.title"))
          )
        }
      }
    }

    "there is not an email in session" should {

      def show: WSResponse = get(verifyContactPrefPath, formatCurrentContactPref(Some(paper)))

      "redirect to the contact preference redirect controller" in {

        given.user.isAuthenticated

        When("the Verify email page is called")
        val result = show

        result should have(
          httpStatus(Status.SEE_OTHER),
          redirectURI(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect()url)
        )
      }
    }

    "the user is an authenticated agent" should {

      def show: WSResponse = get(verifyContactPrefPath, formatEmail(Some(email)))

      "redirect to client VAT account" in {

        given.user.isAuthenticatedAgent

        When("the Verify email page is called")
        val result = show

        result should have(
          httpStatus(Status.SEE_OTHER),
          redirectURI("http://localhost:9149/vat-through-software/representative/client-vat-account")
        )
      }
    }

    "the user is not enrolled for MTD VAT" should {

      def show: WSResponse = get(verifyContactPrefPath, formatEmail(Some(email)))

      "render the unauthorised view" in {

        given.user.isNotEnrolled

        When("the Verify email page is called")
        val result = show

        result should have(
          httpStatus(Status.FORBIDDEN),
          pageTitle(generateDocumentTitle("You can not use this service yet", isAgent = None))
        )
      }
    }
  }

  "Calling the Contact Preference Send Verification route" when {

    "the user is authenticated" when {

      "there is an email in session" should {

        "redirect to the verify email page" in {

          def show: WSResponse = get(contactPrefSendVerificationPath,
            formatEmail(Some(email)) ++ formatCurrentContactPref(Some(paper)) ++ formatInflightChange(Some("false"))
          )

          given.user.isAuthenticated

          And("a successful response from the email verification service is stubbed")
          EmailVerificationStub.stubVerificationRequestSent

          When("The verify email page is called")
          val result = show

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(controllers.email.routes.VerifyEmailController.contactPrefShow().url)
          )
        }

        "return a false from the Email Verification service" should {

          def show: WSResponse = get(contactPrefSendVerificationPath,
            formatEmail(Some(email)) ++ formatCurrentContactPref(Some(paper)) ++ formatInflightChange(Some("false"))
          )

          "redirect to the Confirm Email controller" in {

            given.user.isAuthenticated

            And("a conflict response from the email verification service is stubbed")
            EmailVerificationStub.stubEmailAlreadyVerified

            When("The verify email page is called")
            val result = show

            result should have(
              httpStatus(Status.SEE_OTHER),
              redirectURI(controllers.email.routes.VerifyEmailController.updateContactPrefEmail().url)
            )
          }
        }

        "return None from the Email Verification service" should {

          def show: WSResponse = get(contactPrefSendVerificationPath,
            formatEmail(Some(email)) ++ formatCurrentContactPref(Some(paper)) ++ formatInflightChange(Some("false"))
          )

          "render the internal server error page" in {

            given.user.isAuthenticated

            And("an error from the email verification service is stubbed")
            EmailVerificationStub.stubVerificationRequestError

            When("the verify email page is called")
            val result = show

            result should have(
              httpStatus(Status.INTERNAL_SERVER_ERROR),
              pageTitle(generateDocumentTitle(internalServerErrorTitle))
            )
          }
        }
      }

      "there is not an email in session" should {

        def show: WSResponse = get(contactPrefSendVerificationPath, formatCurrentContactPref(Some(paper)))

        "redirect to the contact preference redirect controller" in {

          given.user.isAuthenticated

          When("the Verify email page is called")
          val result = show

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect().url)
          )
        }
      }
    }

    "the user is an authenticated Agent" should {

      def show: WSResponse = get(contactPrefSendVerificationPath, formatEmail(Some(email)))

      "redirect to client VAT account" in {

        given.user.isAuthenticatedAgent

        When("the Verify email page is called")
        val result = show

        result should have(
          httpStatus(Status.SEE_OTHER),
          redirectURI("http://localhost:9149/vat-through-software/representative/client-vat-account")
        )
      }
    }

    "the user is not enrolled for MTD VAT" should {

      def show: WSResponse = get(contactPrefSendVerificationPath, formatEmail(Some(email)))

      "render the unauthorised view" in {

        given.user.isNotEnrolled

        When("the Verify email page is called")
        val result = show

        result should have(
          httpStatus(Status.FORBIDDEN),
          pageTitle(generateDocumentTitle("You can not use this service yet", isAgent = None))
        )
      }
    }
  }
}
