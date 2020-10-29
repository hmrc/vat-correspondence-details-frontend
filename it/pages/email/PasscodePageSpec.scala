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

import forms.PasscodeForm
import pages.BasePageISpec
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.libs.ws.WSResponse
import stubs.EmailVerificationStub

class PasscodePageSpec extends BasePageISpec {

  val path = "/email-enter-code"

  "Calling the .emailShow action" should {

    "Render the Passcode page" in {

      def show(): WSResponse = get(path, formatInflightChange(Some("false")) ++ formatEmail(Some("test@email.com")))

      given.user.isAuthenticated

      When("I call the passcode page with a validation email in session")

      val res = show()

      res should have(
        httpStatus(OK),
        pageTitle("Enter code to confirm your email address - Business tax account - GOV.UK")
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

    "an incorrect passcode is submitted" should {

      "redirect to manage customer details" in {
        given.user.isAuthenticated

        When("I submit passcode form with the incorrect passcode")
        val res = submit("1234567890")

        res should have(
          httpStatus(BAD_REQUEST),
          elementText("#passcode-error-summary")("Enter the 6 character confirmation code"),
          elementText(".form-field--error .error-message")("Error: Enter the 6 character confirmation code")
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
          elementText("#content > article > p:nth-child(2)")("This is because you have entered the wrong code too many times.")
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
          elementText("#content > article > p:nth-child(2)")("The code we sent you has expired.")
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
        pageTitle("Enter code to confirm your email address - Business tax account - GOV.UK")
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

    "an incorrect passcode is submitted" should {

      "redirect to manage customer details" in {
        given.user.isAuthenticated

        When("I submit passcode form with the incorrect passcode")
        val res = submit("1234567890")

        res should have(
          httpStatus(BAD_REQUEST),
          elementText("#passcode-error-summary")("Enter the 6 character confirmation code"),
          elementText(".form-field--error .error-message")("Error: Enter the 6 character confirmation code")
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
          elementText("#content > article > p:nth-child(2)")("This is because you have entered the wrong code too many times.")
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
          elementText("#content > article > p:nth-child(2)")("The code we sent you has expired.")
        )
      }
    }
  }
}
