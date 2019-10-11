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

package views.errors

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.errors.InFlightChangeView

class InFlightChangeViewSpec extends ViewBaseSpec {

  val injectedView: InFlightChangeView = injector.instanceOf[InFlightChangeView]

  object Selectors {
    val heading = "h1"
    val paragraphOne = "article > p:nth-child(3)"
    val paragraphTwo = "article > p:nth-child(4)"
    val backLink = ".link-back"
    def listItem(num: Int): String = s"#content > article > ul > li:nth-child($num)"
  }

  "The Inflight change pending view" when {

    "the pending change is PPOB" should {

      lazy val view = injectedView("ppob")
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct title" in {
        document.title shouldBe "You already have a change pending - VAT - GOV.UK"
      }

      "have the correct heading" in {
        elementText(Selectors.heading) shouldBe "You already have a change pending"
      }

      "have the correct information in the first paragraph" in {
        elementText(Selectors.paragraphOne) shouldBe
          "You recently requested to change the principal place of business."
      }

      "have the correct information in the second paragraph" in {
        elementText(Selectors.paragraphTwo) shouldBe
          "This change is pending and until this is confirmed, you cannot change your:"
      }

      "have the correct list" which {

        "has the correct first item" in {
          elementText(Selectors.listItem(1)) shouldBe "principal place of business"
        }

        "has the correct second item" in {
          elementText(Selectors.listItem(2)) shouldBe "email address"
        }
      }

      "have the correct text for the back link" in {
        elementText(Selectors.backLink) shouldBe "Back"
      }

      "have the correct back link location" in {
        element(Selectors.backLink).attr("href") shouldBe "mockManageVatOverviewUrl"
      }
    }

    "the pending change is email address" should {

      lazy val view = injectedView("email")
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct information in the first paragraph" in {
        elementText(Selectors.paragraphOne) shouldBe
          "You recently requested to change the business email address."
      }
    }

    "the pending change is landline number" should {

      lazy val view = injectedView("landline")
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct information in the first paragraph" in {
        elementText(Selectors.paragraphOne) shouldBe
          "You recently requested to change the business landline number."
      }
    }

    "the pending change is mobile number" should {

      lazy val view = injectedView("mobile")
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct information in the first paragraph" in {
        elementText(Selectors.paragraphOne) shouldBe
          "You recently requested to change the business mobile number."
      }
    }

    "the pending change is website address" should {

      lazy val view = injectedView("website")
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct information in the first paragraph" in {
        elementText(Selectors.paragraphOne) shouldBe
          "You recently requested to change the business website address."
      }
    }
  }
}
