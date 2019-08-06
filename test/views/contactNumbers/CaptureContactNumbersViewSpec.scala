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
import forms.ContactNumbersForm.contactNumbersForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.contactNumbers.CaptureContactNumbersView

class CaptureContactNumbersViewSpec extends ViewBaseSpec {

  val injectedView: CaptureContactNumbersView = injector.instanceOf[CaptureContactNumbersView]

  "The Capture Contact Numbers page" when {

    "there are no errors in the form" should {

      val view = injectedView(contactNumbersForm(testValidationLandline, testValidationMobile))
      implicit val document: Document = Jsoup.parse(view.body)

      "have the correct title" in {
        document.title shouldBe "Change telephone numbers"
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

        //TODO implement as part of wiring up task
        "has the correct link location" in {

        }
      }
    }

    //TODO implement as part of the form validation task
    "there are errors in the form" should {
//
//      val view = injectedView(contactNumbersForm(testValidationLandline, testValidationMobile).bind())
//      implicit val document: Document = Jsoup.parse(view.body)
    }
  }
}
