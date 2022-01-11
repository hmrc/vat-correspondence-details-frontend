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

package pages.email

import assets.BaseITConstants.internalServerErrorTitle
import forms.PasscodeForm
import pages.BasePageISpec
import play.api.http.Status._
import play.api.libs.ws.WSResponse
import stubs.EmailVerificationStub

class PasscodePageSpec extends BasePageISpec {

  val sendPasscodePath = "/send-passcode"
  val path = "/email-enter-code"
  val email = "test@test.com"

  "Calling the .emailShow action" should {

    "Render the Passcode page" in {

      def show(): WSResponse = get(path, formatInflightChange(Some("false")) ++ formatEmail(Some("test@email.com")))

      given.user.isAuthenticated

      When("I call the passcode page with a validation email in session")

      val res = show()

      res should have(
        httpStatus(OK),
        pageTitle("Enter code to confirm your email address - Manage your VAT account - GOV.UK")
      )
    }
  }

  "Calling the .emailSubmit action" when {

    def submit(passcode: String): WSResponse = post(
      path,
      formatInflightChange(Some("false")) ++ formatEmail(Some("test@email.com"))
    )(toFormData(PasscodeForm.form, passcode))


    "the correct passcode is submitted" should {

      "redirect to the confirmation screen" in {
        given.user.isAuthenticated
        EmailVerificationStub.stubVerifyPasscodeCreated

        When("I submit the passcode form with the correct passcode")
        val res = submit("123456")

        res should have(
          httpStatus(SEE_OTHER),
          pageTitle("")
        )
      }
    }

    "an invalid passcode is submitted" should {

      "reload the page with a form error" in {
        given.user.isAuthenticated

        When("I submit the passcode form with an invalid passcode")
        val res = submit("1234567890")

        res should have(
          httpStatus(BAD_REQUEST),
          elementText(".govuk-error-summary__body")("Enter the 6 character confirmation code"),
          elementText("#passcode-error")("Error: Enter the 6 character confirmation code")
        )
      }
    }

    "too many incorrect passcodes are submitted" should {

      "show the passcode error view" in {
        given.user.isAuthenticated
        EmailVerificationStub.stubPasscodeAttemptsExceeded

        When("I submit the passcode form with an incorrect passcode several times")

        val res = submit("444444")

        res should have(
          httpStatus(BAD_REQUEST),
          elementText("#content > p:nth-child(2)")("This is because you have entered the wrong code too many times.")
        )
      }
    }

    "the passcode was not found or has expired" should {

      "show the passcode error view" in {
        given.user.isAuthenticated
        EmailVerificationStub.stubVerifyPasscodeNotFound

        When("I submit the passcode form with an expired passcode")

        val res = submit("444444")

        res should have(
          httpStatus(BAD_REQUEST),
          elementText("#content > p:nth-child(2)")("The code we sent you has expired.")
        )
      }
    }

    "an incorrect passcode is submitted" should {

      "reload the page with a form error" in {
        given.user.isAuthenticated
        EmailVerificationStub.stubIncorrectPasscode

        When("I submit the passcode form with an incorrect passcode")
        val res = submit("123456")

        res should have(
          httpStatus(BAD_REQUEST),
          elementText(".govuk-error-summary__body")("Enter the 6 character confirmation code"),
          elementText("#passcode-error")("Error: Enter the 6 character confirmation code")
        )
      }
    }
  }

  val contactPrefPath = s"/contact-preference$path"

  "Calling the .contactPrefShow action" should {

    "Render the Passcode page" in {

      def show(): WSResponse = get(
        contactPrefPath,
        formatInflightChange(Some("false")) ++ formatEmail(Some("test@email.com")) ++ formatCurrentContactPref(Some("paper")))

      given.user.isAuthenticated

      When("I call the passcode page with a validation email in session")

      val res = show()

      res should have(
        httpStatus(OK),
        pageTitle("Enter code to confirm your email address - Manage your VAT account - GOV.UK")
      )
    }
  }

  "Calling the .contactPrefSubmit action" when {

    def submit(passcode: String): WSResponse = post(
      contactPrefPath,
      formatInflightChange(Some("false")) ++ formatEmail(Some("test@email.com")) ++ formatCurrentContactPref(Some("paper"))
    )(toFormData(PasscodeForm.form, passcode))


    "the correct passcode is submitted" should {

      "redirect to the confirmation screen" in {
        given.user.isAuthenticated
        EmailVerificationStub.stubVerifyPasscodeCreated

        When("I submit the passcode form with the correct passcode")
        val res = submit("123456")

        res should have(
          httpStatus(SEE_OTHER),
          pageTitle("")
        )
      }
    }

    "an invalid passcode is submitted" should {

      "reload the page with a form error" in {
        given.user.isAuthenticated

        When("I submit the passcode form with an invalid passcode")
        val res = submit("1234567890")

        res should have(
          httpStatus(BAD_REQUEST),
          elementText(".govuk-error-summary__body")("Enter the 6 character confirmation code"),
          elementText("#passcode-error")("Error: Enter the 6 character confirmation code")
        )
      }
    }

    "too many incorrect passcodes are submitted" should {

      "show the passcode error view" in {
        given.user.isAuthenticated
        EmailVerificationStub.stubPasscodeAttemptsExceeded

        When("I submit the passcode form with an incorrect passcode several times")

        val res = submit("444444")

        res should have(
          httpStatus(BAD_REQUEST),
          elementText("#content > p:nth-child(2)")("This is because you have entered the wrong code too many times.")
        )
      }
    }

    "the passcode was not found or has expired" should {

      "show the passcode error view" in {
        given.user.isAuthenticated
        EmailVerificationStub.stubVerifyPasscodeNotFound

        When("I submit the passcode form with an expired passcode")

        val res = submit("444444")

        res should have(
          httpStatus(BAD_REQUEST),
          elementText("#content > p:nth-child(2)")("The code we sent you has expired.")
        )
      }
    }

    "an incorrect passcode is submitted" should {

      "reload the page with a form error" in {
        given.user.isAuthenticated
        EmailVerificationStub.stubIncorrectPasscode

        When("I submit the passcode form with an incorrect passcode")
        val res = submit("123456")

        res should have(
          httpStatus(BAD_REQUEST),
          elementText(".govuk-error-summary__body")("Enter the 6 character confirmation code"),
          elementText("#passcode-error")("Error: Enter the 6 character confirmation code")
        )
      }
    }
  }

  "Calling the .emailSendVerification action" when {

    "there is an email in session" when {

      def request: WSResponse = get(sendPasscodePath, formatEmail(Some(email)))

      "the email verification service returns Some(true)" should {

        "redirect to the verify passcode page" in {

          given.user.isAuthenticated

          And("a successful response for an individual is stubbed")
          EmailVerificationStub.stubPasscodeVerificationRequestSent

          When("the verify passcode page is called")
          val result = request

          result should have(
            httpStatus(SEE_OTHER),
            redirectURI(controllers.email.routes.VerifyPasscodeController.emailShow.url)
          )
        }
      }

      "the email verification service returns Some(false)" should {

        def request: WSResponse = get(sendPasscodePath, formatEmail(Some(email)))

        "redirect to the updateEmailAddress action" in {

          given.user.isAuthenticated

          And("a conflict response comes back from the verification service")
          EmailVerificationStub.stubPasscodeEmailAlreadyVerified

          When("The verify email page is called")
          val result = request

          result should have(
            httpStatus(SEE_OTHER),
            redirectURI(controllers.email.routes.VerifyPasscodeController.updateEmailAddress.url)
          )
        }
      }

      "the email verification service returns None" should {

        def request: WSResponse = get(sendPasscodePath, formatEmail(Some(email)))

        "render the internal server error page" in {

          given.user.isAuthenticated

          And("an error from the email verification service is stubbed")
          EmailVerificationStub.stubPasscodeRequestError

          When("the verify passcode page is called")
          val result = request

          result should have(
            httpStatus(INTERNAL_SERVER_ERROR),
            pageTitle(generateDocumentTitle(internalServerErrorTitle))
          )
        }
      }
    }

    "there isn't an email in session" should {

      def request: WSResponse = get(sendPasscodePath, formatEmail(None))

      "redirect to the capture email address page" in {

        given.user.isAuthenticated

        When("the verify passcode page is called")
        val result = request

        result should have(
          httpStatus(SEE_OTHER),
          redirectURI(controllers.email.routes.CaptureEmailController.show.url))
      }
    }
  }
}
