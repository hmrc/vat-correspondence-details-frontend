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

package views.contactNumbers

import assets.BaseTestConstants.{testValidationLandline, testValidationMobile}
import controllers.contactNumbers.routes
import forms.ContactNumbersForm.contactNumbersForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.contactNumbers.CaptureContactNumbersView

class CaptureContactNumbersViewSpec extends ViewBaseSpec {

  val injectedView: CaptureContactNumbersView = injector.instanceOf[CaptureContactNumbersView]

  "The Capture Contact Numbers page" when {

    "the user is principle entity" when {

      "there are no errors in the form" should {

        val view = injectedView(contactNumbersForm(testValidationLandline, testValidationMobile))(user, messages, mockConfig)
        implicit val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe "Change telephone numbers - Business tax account - GOV.UK"
        }

        "have the correct heading" in {
          elementText("h1") shouldBe "Change telephone numbers"
        }

        "have the correct instruction paragraph" in {
          elementText("#content > article > p") shouldBe
            "Include the country code for international telephone numbers, for example '+44'."
        }

        "have the correct form label for landline number" in {
          elementText("#content > article > form > fieldset > div > div:nth-child(1) > label > span") shouldBe
            "Landline number"
        }

        "have the correct form label for mobile number" in {
          elementText("#content > article > form > fieldset > div > div:nth-child(2) > label > span") shouldBe
            "Mobile number"
        }

        "have a button" which {

          "has the correct text" in {
            elementText(".button") shouldBe "Continue"
          }

          "has the correct link location" in {
            element("form").attr("action") shouldBe routes.CaptureContactNumbersController.submit().url
          }
        }
      }

      "there are errors in the form" should {
        val view = injectedView(contactNumbersForm(testValidationLandline, testValidationMobile)
          .withError("landlineNumber", "Enter a different phone number"))(user, messages, mockConfig)
        implicit val document: Document = Jsoup.parse(view.body)
        "have the correct document title" in {
          document.title shouldBe "Error: Change telephone numbers - Business tax account - GOV.UK"
        }

        "have a form error box" which {

          "has the correct error message" in {
            elementText("#landlineNumber-error-summary") shouldBe "Enter a different phone number"
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


    "the user is an agent" should {

      "there are no errors in the form" should {
        val view = injectedView(contactNumbersForm(testValidationLandline, testValidationMobile))(agent, messages, mockConfig)
        implicit val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe "Change telephone numbers - Your client’s VAT details - GOV.UK"
        }
      }
    }
  }
}
