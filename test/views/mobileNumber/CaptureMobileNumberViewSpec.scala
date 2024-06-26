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

package views.mobileNumber

import assets.BaseTestConstants.testValidationMobile
import controllers.mobileNumber.routes
import forms.MobileNumberForm.mobileNumberForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import views.ViewBaseSpec
import views.html.mobileNumber.CaptureMobileNumberView

class CaptureMobileNumberViewSpec extends ViewBaseSpec with Matchers {

  val injectedView: CaptureMobileNumberView = inject[CaptureMobileNumberView]

  "The Capture Mobile Number page" when {

    "the user is not an agent" when {

      "there are no errors in the form" should {

        lazy val view = injectedView(mobileNumberForm(testValidationMobile),testValidationMobile)(user, messages, mockConfig)
        implicit lazy val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe "What is the mobile number? - Manage your VAT account - GOV.UK"
        }

        "have the correct heading" in {
          elementText("h1") shouldBe "What is the mobile number?"
        }

        "have a back link" which {

          "should have the correct text" in {
            elementText(".govuk-back-link") shouldBe "Back"
          }

          "should have the correct href" in {
            element(".govuk-back-link").attr("href") shouldBe mockConfig.btaAccountDetailsUrl
          }
        }

        "have the correct field hint" in {
          elementText(".govuk-hint") shouldBe
            "You need to enter the country code for international numbers, like 00447946 123456. You cannot enter a plus sign."
        }

        "have a link to remove the mobile" which {

          "has the correct text" in {
            elementText("#remove-mobile") shouldBe "Remove mobile number"
          }

          "has the correct link location" in {
            element("#remove-mobile").attr("href") shouldBe routes.ConfirmMobileNumberController.removeShow.url
          }
        }

        "have a button" which {

          "has the correct text" in {
            elementText(".govuk-button") shouldBe "Continue"
          }

          "has the correct link location" in {
            element("form").attr("action") shouldBe routes.CaptureMobileNumberController.submit.url
          }
        }

        "have the HMRC Privacy Notice" in {
          element("#hmrc-privacy-notice a").attr("href") shouldBe mockConfig.hmrcPrivacyNoticeUrl
        }
      }

      "there are errors in the form" should {
        lazy val view = injectedView(mobileNumberForm(testValidationMobile)
          .withError("mobileNumber", messages("captureMobile.error.notChanged")),testValidationMobile)(user, messages, mockConfig)
        implicit lazy val document: Document = Jsoup.parse(view.body)

        "have the correct document title" in {
          document.title shouldBe "Error: What is the mobile number? - Manage your VAT account - GOV.UK"
        }

        "have a form error box" which {

          "has the correct error message" in {
            elementText(".govuk-error-summary__list > li > a") shouldBe "You have not made any changes to the mobile number"
          }
        }

        "have the correct error notification text above the input box" in {
          elementText(".govuk-error-message") shouldBe "Error: You have not made any changes to the mobile number"
        }

        "display the error summary" in {
          element(".govuk-error-summary__title").text() shouldBe "There is a problem"
        }
      }
    }

    "the user is an agent" when {

      "there are no errors in the form" should {
        lazy val view = injectedView(mobileNumberForm(testValidationMobile),testValidationMobile)(agent, messages, mockConfig)
        implicit lazy val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe "What is the mobile number? - Your client’s VAT details - GOV.UK"
        }

        "have a back link" which {

          "should have the correct href" in {
            element(".govuk-back-link").attr("href") shouldBe mockConfig.manageVatSubscriptionServicePath
          }
        }
      }
    }
  }
}
