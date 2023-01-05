/*
 * Copyright 2023 HM Revenue & Customs
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

import controllers.website.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import views.ViewBaseSpec
import views.html.website.ConfirmRemoveWebsiteView
import forms.YesNoForm
import models.YesNo
import play.api.data.Form

class ConfirmRemoveWebsiteViewSpec extends ViewBaseSpec with Matchers {

  val injectedView: ConfirmRemoveWebsiteView = injector.instanceOf[ConfirmRemoveWebsiteView]
  val form: Form[YesNo] = YesNoForm.yesNoForm("confirmWebsiteRemove.error")

  object Selectors {
    val heading = "h1"
    val backLink = ".govuk-back-link"
    val continueButton = ".govuk-button"
    val yesOption = "#label-yes"
    val noOption = "#label-no"
    val errorHeading = ".govuk-error-summary h2"
    val error = ".govuk-error-message"
    val errorList = "#content > div > div > ul > li > a"
  }


  "The Confirm Website view" when {

    "the user is a principal entity" should {

      lazy val view = injectedView(form)(user, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct document title" in {
        document.title shouldBe "Are you sure you want to remove the website address? - Manage your VAT account - GOV.UK"
      }

      "have the correct heading" in {
        elementText(Selectors.heading) shouldBe "Are you sure you want to remove the website address?"
      }

      "have a back link" which {

        "should have the correct text" in {
          elementText(Selectors.backLink) shouldBe "Back"
        }

        "should have the correct back link" in {
          element(Selectors.backLink).attr("href") shouldBe controllers.website.routes.CaptureWebsiteController.show.url
        }
      }

      "have a form" which {

        "when an option is selected" should {

          "has the correct action" in {
            element("form").attr("action") shouldBe routes.ConfirmWebsiteController.removeWebsiteAddress.url
          }

          "has a Yes option" in {
            elementText(Selectors.yesOption) shouldBe "Yes"
          }

          "has a No option" in {
            elementText(Selectors.noOption) shouldBe "No"
          }

          "has a continue button with the correct text" in {
            elementText(Selectors.continueButton) shouldBe "Confirm and continue"
          }

          "have a prevent double click attribute on the button" in {
            element(Selectors.continueButton).hasAttr("data-prevent-double-click") shouldBe true
          }
        }

        "when the page has errors" should {

          val errorForm = YesNoForm.yesNoForm("confirmWebsiteRemove.error").bind(Map(YesNoForm.yesNo -> ""))
          lazy val view = injectedView(errorForm)(user, messages, mockConfig)
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the correct document title" in {
            document.title shouldBe "Error: Are you sure you want to remove the website address? - Manage your VAT account - GOV.UK"
          }

          "have the correct page heading" in {
            elementText(Selectors.heading) shouldBe "Are you sure you want to remove the website address?"
          }

          "display the correct error heading" in {
            elementText(Selectors.errorHeading) shouldBe "There is a problem"
          }

          "has the correct error text" in {

            elementText(Selectors.error) shouldBe "Error: Select yes if you want to remove the website address"
          }

          "has the correct link to the radio option" in {

            element(Selectors.errorList).attr("href") shouldBe "#yes_no-yes"
          }

        }
      }
      }
    }

    "the user is an agent" should {

      "there are no errors in the form" should {
        val view = injectedView(form)(agent, messages, mockConfig)
        implicit val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe "Are you sure you want to remove the website address? - Your client’s VAT details - GOV.UK"
        }
      }
    }

}
