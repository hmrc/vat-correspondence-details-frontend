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

package views.mobileNumber

import assets.BaseTestConstants.testValidationMobile
import controllers.mobileNumber.routes
import forms.MobileNumberForm.mobileNumberForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.mobileNumber.CaptureMobileNumberView

class CaptureMobileNumberViewSpec extends ViewBaseSpec {

  val injectedView: CaptureMobileNumberView = inject[CaptureMobileNumberView]

  "The Capture Mobile Number page" when {

    "the user is not an agent" when {

      "there are no errors in the form" should {

        val view = injectedView(mobileNumberForm(testValidationMobile))(user, messages, mockConfig)
        implicit val document: Document = Jsoup.parse(view.body)
        val fieldLabel: String = "#content > article > form > fieldset > div > label "

        "have the correct title" in {
          document.title shouldBe "What is the mobile number? - Business tax account - GOV.UK"
        }

        "have the correct heading" in {
          elementText("h1") shouldBe "What is the mobile number?"
        }

        "have the correct field hint" in {
          elementText(s"$fieldLabel > span.form-hint") shouldBe
            "You will need to include the country code for international telephone numbers, for example '+44'."
        }

        "have the correct visually hidden text" in {
          elementText(s"$fieldLabel > span.visuallyhidden") shouldBe "What is the mobile number?"
        }

        "have a button" which {

          "has the correct text" in {
            elementText(".button") shouldBe "Continue"
          }

          "has the correct link location" in {
            element("form").attr("action") shouldBe routes.CaptureMobileNumberController.submit().url
          }
        }
      }

      "there are errors in the form" should {
        val view = injectedView(mobileNumberForm(testValidationMobile)
          .withError("mobileNumber", "Enter a different phone number"))(user, messages, mockConfig)
        implicit val document: Document = Jsoup.parse(view.body)

        "have the correct document title" in {
          document.title shouldBe "Error: What is the mobile number? - Business tax account - GOV.UK"
        }

        "have a form error box" which {

          "has the correct error message" in {
            elementText("#mobileNumber-error-summary") shouldBe "Enter a different phone number"
          }
        }

        "have the correct error notification text above the input box" in {
          elementText(".error-notification") shouldBe "Enter a different phone number"
        }

        "display the error summary" in {
          element("#error-summary-heading").text() shouldBe "There is a problem"
        }
      }
    }

    "the user is an agent" when {

      "there are no errors in the form" should {
        val view = injectedView(mobileNumberForm(testValidationMobile))(agent, messages, mockConfig)
        implicit val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe "What is the mobile number? - Your client’s VAT details - GOV.UK"
        }
      }
    }
  }
}
