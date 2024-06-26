/*
 * Copyright 2024 HM Revenue & Customs
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

package views.email

import assets.BaseTestConstants.vrn
import common.SessionKeys
import controllers.email.routes
import forms.PasscodeForm
import models.User
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import play.api.data.Form
import play.twirl.api.Html
import views.ViewBaseSpec
import views.html.email.PasscodeView

class PasscodeViewSpec extends ViewBaseSpec with Matchers {

  val injectedView: PasscodeView = inject[PasscodeView]
  val testForm: Form[String] = PasscodeForm.form

  "The passcode view in the change of email journey" when {

    "the user has no session keys to determine back link location" should {

      lazy val view: Html = injectedView(testEmail, testForm, contactPrefJourney = false)(user, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct document title" in {
        document.title shouldBe "Enter code to confirm your email address - Manage your VAT account - GOV.UK"
      }

      "have the correct heading" in {
        elementText("h1") shouldBe "Enter code to confirm your email address"
      }

      "have a back link" which {

        "has the correct text" in {
          elementText(".govuk-back-link") shouldBe "Back"
        }

        "has the correct destination" in {
          element(".govuk-back-link").attr("href") shouldBe controllers.email.routes.CaptureEmailController.show.url
        }
      }

      "have the correct first paragraph" in {
        elementText("div > div > div > p:nth-of-type(1)") shouldBe s"We have sent a code to: $testEmail"
      }

      "have the correct panel paragraph" in {
        elementText(".govuk-inset-text") shouldBe "Open a new tab or window if you need to access your emails online."
      }

      "have the correct label for the form" in {
        elementText("form .govuk-label") shouldBe "Confirmation code"
      }

      "have the correct form hint" in {
        elementText("form .govuk-hint") shouldBe "For example, DNCLRK"
      }

      "have the correct progressive disclosure text" in {
        elementText(".govuk-details__summary-text") shouldBe "I have not received the email"
      }

      "have the correct first paragraph inside of the progressive disclosure" in {
        elementText(".govuk-details__text > p:nth-of-type(1)") shouldBe
          "The email might take a few minutes to arrive. Its subject line is: ‘Confirm your email address - VAT account’."
      }

      "have the correct second paragraph inside of the progressive disclosure" in {
        elementText(".govuk-details__text > p:nth-of-type(2)") shouldBe "Check your spam or junk folder. " +
          "If the email has still not arrived, you can request a new code or provide another email address."
      }

      "have a link to resend the passcode" which {

        "has the correct text" in {
          elementText(".govuk-details__text > p:nth-child(2) a:nth-child(1)") shouldBe "request a new code"
        }

        "has the correct destination" in {
          element(".govuk-details__text > p:nth-of-type(2) > a:nth-child(1)").attr("href") shouldBe
            routes.VerifyPasscodeController.emailSendVerification.url
        }
      }

      "have a link to enter a new email address" which {

        "has the correct text" in {
          elementText(".govuk-details__text > p:nth-of-type(2) > a:nth-child(2)") shouldBe "provide another email address"
        }

        "has the correct destination" in {
          element(".govuk-details__text > p:nth-of-type(2) > a:nth-child(2)").attr("href") shouldBe
            routes.CaptureEmailController.show.url
        }
      }

      "have a form with the correct action" in {
        element("form").attr("action") shouldBe "/vat-through-software/account/correspondence/email-enter-code"
      }

      "have a button with the correct text" in {
        elementText(".govuk-button") shouldBe "Continue"
      }

      "have the prevent double click attribute on the continue button" in {
        element(".govuk-button").hasAttr("data-prevent-double-click") shouldBe true
      }
    }

    "the user has the relevant session key to show they have come from the 'fix your email' page" should {

      val user = User(vrn)(getRequest.withSession(SessionKeys.manageVatRequestToFixEmail -> "false"))
      lazy val view: Html = injectedView(testEmail, testForm, contactPrefJourney = false)(user, messages, mockConfig)

      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have a back link to the 'fix your email' page" in {
        element(".govuk-back-link").attr("href") shouldBe controllers.email.routes.BouncedEmailController.show.url
      }
    }
  }

  "The passcode view in the change of contact preference journey" should {

    lazy val view: Html = injectedView(testEmail, testForm, contactPrefJourney = true)(user, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have a back link" which {

      "has the correct destination" in {
        element(".govuk-back-link").attr("href") shouldBe controllers.email.routes.CaptureEmailController.showPrefJourney.url
      }
    }

    "have a link to resend the passcode" which {

      "has the correct destination" in {
        element("details div p:nth-child(2) a:nth-child(1)").attr("href") shouldBe
          routes.VerifyPasscodeController.contactPrefSendVerification.url
      }
    }

    "have a link to enter a new email address" which {

      "has the correct destination" in {
        element("details div p:nth-child(2) a:nth-child(2)").attr("href") shouldBe
          routes.CaptureEmailController.showPrefJourney.url
      }
    }
  }

  "The passcode view when there are errors in the form" should {

    val errorForm = testForm.bind(Map("passcode" -> "F"))
    lazy val view: Html = injectedView(testEmail, errorForm, contactPrefJourney = true)(user, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe "Error: Enter code to confirm your email address - Manage your VAT account - GOV.UK"
    }

    "have an error summary" which {

      "has the correct heading" in {
        elementText(".govuk-error-summary__title") shouldBe "There is a problem"
      }

      "has the correct error message" in {
        elementText(".govuk-error-summary__body") shouldBe "Enter the 6 character confirmation code"
      }

      "has a link to the input with the error" in {
        element(".govuk-error-summary__body a").attr("href") shouldBe "#passcode"
      }
    }

    "have the correct error notification text above the input box" in {
      elementText("#passcode-error") shouldBe "Error: Enter the 6 character confirmation code"
    }
  }
}
