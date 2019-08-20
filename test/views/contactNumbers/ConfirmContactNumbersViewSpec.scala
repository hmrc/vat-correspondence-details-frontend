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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.contactNumbers.ConfirmContactNumbersView
import assets.BaseTestConstants._
import controllers.contactNumbers.routes

class ConfirmContactNumbersViewSpec extends ViewBaseSpec {

  val injectedView: ConfirmContactNumbersView = injector.instanceOf[ConfirmContactNumbersView]

  object Selectors {
    val heading = "h1"
    val backLink = "#content > article > a"
    val continueButton = ".button"
    val editLink = "#content > article > p:nth-child(4) > a"
    val newPhoneNumbers = "#content > article > p"
    val newPhoneNumbersBreak = "#content > article > p > br"
  }

  "The Confirm Contact Numbers view" when {

    "the user is  principle entity" should {

      lazy val view = injectedView(testPrepopLandline, testPrepopMobile)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct title" in {
        document.title shouldBe "Confirm telephone number changes - Business tax account - GOV.UK"
      }

      "have the correct heading" in {
        elementText(Selectors.heading) shouldBe "Confirm telephone number changes"
      }

      "have a back link" which {

        "should have the correct text" in {
          elementText(Selectors.backLink) shouldBe "Back"
        }

        "should have the correct back link" in {
          element(Selectors.backLink).attr("href") shouldBe routes.CaptureContactNumbersController.show().url
        }
      }

      "have the contact numbers the user provided" in {
        elementText(Selectors.newPhoneNumbers) shouldBe s"Landline : $testPrepopLandline Mobile : $testPrepopMobile"
      }

      "have a line break in the telephone number p tag" in {
        elementExists(Selectors.newPhoneNumbersBreak)
      }


      "have a link to edit phone numbers" which {

        "has the correct text" in {
          elementText(Selectors.editLink) shouldBe "Change telephone numbers"
        }

        "has the correct link" in {
          element(Selectors.editLink).attr("href") shouldBe routes.CaptureContactNumbersController.show().url
        }

      }

      "have a continue button" which {

        "has the correct text" in {
          elementText(Selectors.continueButton) shouldBe "Confirm and continue"
        }

        "has the correct link" in {
          element(Selectors.continueButton).attr("href") shouldBe
            routes.ConfirmContactNumbersController.updateContactNumbers().url
        }
      }
    }

    "the user is an agent" should {

      "there are no errors in the form" should {
        val view = injectedView(testPrepopLandline, testPrepopMobile)(agent, messages, mockConfig)
        implicit val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe "Confirm telephone number changes - Your client’s VAT details - GOV.UK"
        }
      }
    }
  }
}
