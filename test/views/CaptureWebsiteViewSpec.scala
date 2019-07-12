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

package views

import forms.WebsiteForm._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.html.CaptureWebsiteView

class CaptureWebsiteViewSpec extends ViewBaseSpec {

  val injectedView: CaptureWebsiteView = injector.instanceOf[CaptureWebsiteView]

  object Selectors {
    val pageHeading = "#content h1"
    val backLink = "#content > article > a"
    val hintText = "#label-website-hint"
    val form = "form"
    val websiteField = "#website"
    val continueButton = "button"
    val errorSummary = "#error-summary-heading"
    val websiteFormGroup = "#content > article > form > div:nth-child(1)"
    val removeWebsite = "#remove-website"
  }

  "Rendering the capture website page" when {

    "the form has no errors" when {

      "the user already has a website in ETMP" should {
        lazy val view: Html = injectedView(websiteForm(testWebsiteAddress).fill(testWebsiteAddress), testWebsiteAddress)

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct document title" in {
          document.title shouldBe "What’s the website address?"
        }

        "have a back link" which {

          "should have the correct text" in {
            elementText(Selectors.backLink) shouldBe "Back"
          }

          "should have the correct back link" in {
            element(Selectors.backLink).attr("href") shouldBe "mockManageVatOverviewUrl"
          }
        }

        "have the correct page heading" in {
          elementText(Selectors.pageHeading) shouldBe "What’s the website address?"
        }

        "have the correct hint text" in {
          elementText(Selectors.hintText) shouldBe "For example, www.abc.co..."
        }

        "have the website form with the correct form action" in {
          element(Selectors.form).attr("action") shouldBe "/vat-through-software/account/" +
            "correspondence/new-website-address"
        }

        "have the website text field with the pre-populated value" in {
          element(Selectors.websiteField).attr("value") shouldBe testWebsiteAddress
        }

        "have the continue button" in {
          elementText(Selectors.continueButton) shouldBe "Continue"
        }

        "show the remove email address link" in {
          elementText(Selectors.removeWebsite) shouldBe "Remove website address"
        }

      }

      "the user has no email address in ETMP" should {
        lazy val view: Html = injectedView(websiteForm(testWebsiteAddress), "")
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the email text field with no pre-populated value" in {
          element(Selectors.websiteField).attr("value") shouldBe ""
        }

        "not show the remove email address link" in {
          elementExtinct(Selectors.removeWebsite)
        }
      }
    }
  }
}

