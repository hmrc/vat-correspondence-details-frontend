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

package views.website

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.website.ConfirmRemoveWebsiteView

class ConfirmRemoveWebsiteViewSpec extends ViewBaseSpec {

  val injectedView: ConfirmRemoveWebsiteView = injector.instanceOf[ConfirmRemoveWebsiteView]

  object Selectors {
    val heading = ".heading-large"
    val backLink = "#content > article > a"
    val continueButton = ".button"
    val cancelLink = ".content__body > p:nth-child(3) > a:nth-child(1)"
  }

  "The Confirm Website view" when {

    "the user is a principle entity" should {

      lazy val view = injectedView(testWebsite)(user, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct document title" in {
        document.title shouldBe "Confirm you want to remove the website address - Business tax account - GOV.UK"
      }

      "have the correct heading" in {
        elementText(Selectors.heading) shouldBe s"Confirm you want to remove the website address: $testWebsite"
      }

      "have a back link" which {

        "should have the correct text" in {
          elementText(Selectors.backLink) shouldBe "Back"
        }

        "should have the correct back link" in {
          element(Selectors.backLink).attr("href") shouldBe controllers.website.routes.CaptureWebsiteController.show().url
        }
      }

      "have a continue button" which {

        "has the correct text" in {
          elementText(Selectors.continueButton) shouldBe "Confirm and continue"
        }
      }
    }
    "the user is an agent" should {

      "there are no errors in the form" should {
        val view = injectedView(testWebsite)(agent, messages, mockConfig)
        implicit val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe "Confirm you want to remove the website address - Your client’s VAT details - GOV.UK"
        }
      }
    }
  }
}
