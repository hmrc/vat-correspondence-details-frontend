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

package views.email

import forms.EmailForm._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import play.twirl.api.Html
import views.ViewBaseSpec
import views.html.email.CaptureEmailView
import play.api.mvc.Call

class CaptureEmailViewSpec extends ViewBaseSpec with Matchers {

  val injectedView: CaptureEmailView = injector.instanceOf[CaptureEmailView]

  private object Selectors {
    val pageHeading = "#content h1"
    val backLink = ".govuk-back-link"
    val form = "form"
    val emailField = "#email"
    val continueButton = ".govuk-button"
    val errorSummary = "#error-summary-title"
    val removeEmail = "summary"
    val removeEmailDesc = "#content > details > div"
    val removeEmailLink = "#content > details > div > a"
    val onlyAddEmail = "#content > form > p"
    val fieldLabel: String = ".govuk-hint"
    val hmrcPrivacyNotice: String = "#hmrc-privacy-notice"
    val hmrcPrivacyNoticeLink: String = "#hmrc-privacy-notice a"
  }

  "Rendering the capture email page" when {

    "the letterToConfirmedEmail boolean is false" when {

      "the form has no errors" when {

        "the user has an email address" should {

          lazy val view: Html = injectedView(emailForm(testEmail).fill(testEmail),
            emailNotChangedError = false,
            currentEmail = testEmail,
            Call("POST", "/vat-through-software/account/correspondence/change-email-address"),
            letterToConfirmedEmail = false
          )(user, messages, mockConfig)

          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the correct document title" in {
            document.title shouldBe "What is the email address? - Manage your VAT account - GOV.UK"
          }

          "have a back link" which {

            "should have the correct text" in {
              elementText(Selectors.backLink) shouldBe "Back"
            }

            "should have the correct href" in {
              element(Selectors.backLink).attr("href") shouldBe mockConfig.btaAccountDetailsUrl
            }
          }

          "have the correct page heading" in {
            elementText(Selectors.pageHeading) shouldBe "What is the email address?"
          }

          "have the correct field hint" in {
            elementText(Selectors.fieldLabel) shouldBe
              "We will use this to send you updates about your VAT account if you have agreed to be contacted by email."
          }

          "have the email form with the correct form action" in {
            element(Selectors.form).attr("action") shouldBe "/vat-through-software/account/" +
              "correspondence/change-email-address"
          }

          "have the email text field with the pre-populated value" in {
            element(Selectors.emailField).attr("value") shouldBe "test@email.co.uk"
          }

          "not have the section about adding email address because the user has an email" in {
            elementExtinct(Selectors.onlyAddEmail)
          }

          "have the continue button" in {
            elementText(Selectors.continueButton) shouldBe "Continue"
          }

          "have the progressive disclosure to remove an email address" which {
            "has the correct heading" in {
              elementText(Selectors.removeEmail) shouldBe "I would like to remove my email address"
            }

            "has the correct description" in {
              elementText(Selectors.removeEmailDesc) shouldBe "Contact us (opens in a new tab) to remove your email address."
            }

            "has the correct link" in {
              element(Selectors.removeEmailLink).attr("href") shouldBe "mockRemoveEmailUrl"
            }
          }

          "have the HMRC Privacy Notice with the correct text" in {
            elementText(Selectors.hmrcPrivacyNotice) shouldBe
              "Full details of how we use your information are in the HMRC Privacy Notice (opens in a new window or tab)."
            element(Selectors.hmrcPrivacyNoticeLink).attr("href") shouldBe mockConfig.hmrcPrivacyNoticeUrl
          }
        }

        "the user has no email address in ETMP" should {
          lazy val view: Html = injectedView(emailForm(testEmail),
            emailNotChangedError = false, currentEmail = "", Call("", ""), letterToConfirmedEmail = false)(user, messages, mockConfig)
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the email text field with no pre-populated value" in {
            element(Selectors.emailField).attr("value") shouldBe ""
          }

          "have a paragraph about adding email address because the user has an email" in {
            elementText(Selectors.onlyAddEmail) shouldBe "Only complete this field if you " +
              "have been told to do so by HMRC."
          }

          "not have the progressive disclosure to remove an email address" in {
            elementExtinct(Selectors.removeEmail)
          }
        }
      }

      "the form has the email unchanged error" should {
        lazy val view = injectedView(emailForm(testEmail).bind(Map("email" -> testEmail)),
          emailNotChangedError = true, currentEmail = testEmail, Call("", ""), letterToConfirmedEmail = false)(user, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct document title" in {
          document.title shouldBe "Error: What is the email address? - Manage your VAT account - GOV.UK"
        }

        "have a form error box" which {

          "has the correct error message" in {
            elementText(".govuk-error-summary__body") shouldBe "Enter a different email address"
          }
        }

        "have the correct error notification text above the input box" in {
          elementText(".govuk-error-message") shouldBe "Error: Enter a different email address"
        }

        "display the error summary" in {
          element(Selectors.errorSummary).text() shouldBe "There is a problem"
        }

        "not have the section about adding email address because the user has an email" in {
          elementExtinct(Selectors.onlyAddEmail)
        }
      }
    }


    "the letterToConfirmedEmail boolean when set to true" when {

      "a user has an email address" should {
        lazy val view: Html = injectedView(emailForm(testEmail).fill(testEmail),
          emailNotChangedError = false,
          currentEmail = testEmail,
          Call("POST", "/vat-through-software/account/correspondence/change-email-address"),
          letterToConfirmedEmail = true
        )(user, messages, mockConfig)

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have a back link" which {

          "directs to the Email to use page" in {
            element(Selectors.backLink).attr("href") shouldBe "/vat-through-software/account/correspondence/preference-confirm-email"
          }
        }
      }

      "a user does not have an email address" should {
        lazy val view: Html = injectedView(emailForm(testEmail).fill(testEmail),
          emailNotChangedError = false,
          currentEmail = "",
          Call("POST", "/vat-through-software/account/correspondence/change-email-address"),
          letterToConfirmedEmail = true
        )(user, messages, mockConfig)

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have a back link" which {

          "directs to the 'We do not have an email address for you' page" in {
            element(Selectors.backLink).attr("href") shouldBe "/vat-through-software/account/correspondence/contact-preference/add-email-address"
          }
        }
      }
    }
  }
}

