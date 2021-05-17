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

package views.website

import forms.WebsiteForm._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.ViewBaseSpec
import views.html.website.CaptureWebsiteView

class CaptureWebsiteViewSpec extends ViewBaseSpec {

  val injectedView: CaptureWebsiteView = injector.instanceOf[CaptureWebsiteView]

  private object Selectors {
    val pageHeading = "h1"
    val formHint: String = ".govuk-hint"
    val backLink = ".govuk-back-link"
    val form = "form"
    val websiteField = "#website"
    val continueButton = ".govuk-button"
    val removeWebsite = "#remove-website"
  }

  "Rendering the capture website page" when {

    "the user is a principle entity" when {

      "the form has no errors" should {

        "the user already has a website in ETMP" should {
          lazy val view: Html =
            injectedView(websiteForm(testWebsite).fill(testWebsite), testWebsite)(user, messages, mockConfig)

          implicit lazy val document: Document = Jsoup.parse(view.body)

          "have the correct document title" in {
            document.title shouldBe "What’s the website address? - Business tax account - GOV.UK"
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
            elementText(Selectors.pageHeading) shouldBe "What’s the website address?"
          }

          "have the correct hint text" in {
            elementText(Selectors.formHint) shouldBe "For example, www.abc.co..."
          }

          "have the website form with the correct form action" in {
            element(Selectors.form).attr("action") shouldBe "/vat-through-software/account/" +
              "correspondence/new-website-address"
          }

          "have the website text field with the pre-populated value" in {
            element(Selectors.websiteField).attr("value") shouldBe testWebsite
          }

          "have the continue button" in {
            elementText(Selectors.continueButton) shouldBe "Continue"
          }

          "show the remove website link" in {
            elementText(Selectors.removeWebsite) shouldBe "Remove website address"
          }

          "have the correct remove website link" in {
            element(Selectors.removeWebsite).attr("href") shouldBe
              controllers.website.routes.ConfirmRemoveWebsiteController.show().url
          }
        }

        "the user has no website in ETMP" should {
          lazy val view: Html = injectedView(websiteForm(testWebsite), "")(user, messages, mockConfig)
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the website text field with no pre-populated value" in {
            element(Selectors.websiteField).attr("value") shouldBe ""
          }

          "not show the remove website link" in {
            elementExtinct(Selectors.removeWebsite)
          }
        }
      }

      "there are errors in the form" should {
        lazy val view = injectedView(websiteForm(testWebsite).bind(
          Map("website" -> testWebsite)
        ), testWebsite)(user, messages, mockConfig)
        implicit lazy val document: Document = Jsoup.parse(view.body)

        "have the correct document title" in {
          document.title shouldBe "Error: What’s the website address? - Business tax account - GOV.UK"
        }

        "have a form error box" which {

          "has the correct error message" in {
            elementText(".govuk-error-summary__list > li > a") shouldBe "Enter a new website address"
          }
        }

        "have the correct error notification text above the input box" in {
          elementText(".govuk-error-message") shouldBe "Error: Enter a new website address"
        }

        "display the error summary" in {
          element("#error-summary-title").text() shouldBe "There is a problem"
        }
      }

      "the BTA entry point feature is set to false" should {

        lazy val view: Html = {
          mockConfig.features.btaEntryPointEnabled(false)
          injectedView(websiteForm(testWebsite).fill(testWebsite), testWebsite)(user, messages, mockConfig)
        }
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have a back link" which {

          "should have the correct href" in {
            element(Selectors.backLink).attr("href") shouldBe mockConfig.manageVatSubscriptionServicePath
          }
        }
      }
    }

    "the user is an agent" should {

      "there are no errors in the form" should {
        lazy val view = injectedView(websiteForm(testWebsite).fill(testWebsite), testWebsite)(agent, messages, mockConfig)
        implicit lazy val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe "What’s the website address? - Your client’s VAT details - GOV.UK"
        }

        "have a back link" which {

          "should have the correct href" in {
            element(Selectors.backLink).attr("href") shouldBe mockConfig.manageVatSubscriptionServicePath
          }
        }
      }
    }
  }
}

