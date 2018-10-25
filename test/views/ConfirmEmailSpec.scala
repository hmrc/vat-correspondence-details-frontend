/*
 * Copyright 2018 HM Revenue & Customs
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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class ConfirmEmailSpec extends ViewBaseSpec {

  val testEmail: String = "test@email.com"

  object Selectors {
    val heading = "heading-large"
    val backLink = "#content > article > a"
    val continueButton = ".button"
    val editLink = "#content > article > p:nth-child(4) > a"
  }

  "The Confirm Email view" should {
    lazy val view = views.html.confirm_email(testEmail)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct heading" in {
      document.getElementsByClass(Selectors.heading).text() shouldBe "Confirm the new email address"
    }

    "have a back link" which {

      "should have the correct text" in {
        elementText(Selectors.backLink) shouldBe "Back"
      }

      "should have the correct back link" in {
        element(Selectors.backLink).attr("href") shouldBe controllers.routes.CaptureEmailController.show().url
      }
    }

    "have the email address the user provided" in {
      document.text() contains testEmail
    }

    "have a link to edit email address" which {

      "has the correct text" in {
        elementText(Selectors.editLink) shouldBe "Edit email address"
      }

      "has the correct link" in {
        element(Selectors.editLink).attr("href") shouldBe controllers.routes.CaptureEmailController.show().url
      }

      "has the correct GA tag" in {
        element(Selectors.editLink).attr("data-journey-click") shouldBe "email-address:edit:confirm-email"
      }
    }

    "have some body text" in {
      document.text() contains "By confirming this change, you agree that the information you have given is complete and correct."
    }

    "have a continue button" which {

      "has the correct text" in {
        elementText(Selectors.continueButton) shouldBe "Confirm and continue"
      }

      "has the correct GA tag" in {
        element(Selectors.continueButton).attr("data-journey-click") shouldBe "email-address:confirm:confirm-email"
      }
    }
  }
}
