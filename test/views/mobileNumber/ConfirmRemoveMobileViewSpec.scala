/*
 * Copyright 2022 HM Revenue & Customs
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

import controllers.mobileNumber.routes
import forms.YesNoForm
import models.YesNo
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import play.api.data.Form
import views.ViewBaseSpec
import views.html.mobileNumber.ConfirmRemoveMobileView

class ConfirmRemoveMobileViewSpec extends ViewBaseSpec with Matchers {

  val injectedView: ConfirmRemoveMobileView = inject[ConfirmRemoveMobileView]
  val form: Form[YesNo] = YesNoForm.yesNoForm("confirmRemoveMobile.error")

  object Selectors {
    val heading = "h1"
    val form = "form"
    val yesLabel = "#label-yes"
    val noLabel = "#label-no"
    val button = ".govuk-button"
    val errorText = "#yes_no-error"
    val errorSummaryTitle = "#error-summary-title"
    val errorLink = ".error-link"
  }

  "The ConfirmRemoveMobile page" when {

    "there are no errors" when {

      "the user is a principal entity" should {

        lazy val view = injectedView(form)(user, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe
            "Are you sure you want to remove the mobile number? - Manage your VAT account - GOV.UK"
        }

        "have the correct heading" in {
          elementText(Selectors.heading) shouldBe "Are you sure you want to remove the mobile number?"
        }

        "have a form" which {

          "has the correct action" in {
            element(Selectors.form).attr("action") shouldBe routes.ConfirmMobileNumberController.removeMobileNumber.url
          }

          "has a Yes option" in {
            elementText(Selectors.yesLabel) shouldBe "Yes"
          }

          "has a No option" in {
            elementText(Selectors.noLabel) shouldBe "No"
          }

          "has a continue button with the correct text" in {
            elementText(Selectors.button) shouldBe "Confirm and continue"
          }

          "have a prevent double click attribute on the button" in {
            element(Selectors.button).hasAttr("data-prevent-double-click") shouldBe true
          }
        }
      }

      "the user is an agent" should {

        lazy val view = injectedView(form)(agent, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe
            "Are you sure you want to remove the mobile number? - Your client’s VAT details - GOV.UK"
        }
      }
    }

    "there are errors" should {

      val errorForm = YesNoForm.yesNoForm("confirmRemoveMobile.error").bind(Map(YesNoForm.yesNo -> ""))
      lazy val view = injectedView(errorForm)(user, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct title" in {
        document.title shouldBe
          "Error: Are you sure you want to remove the mobile number? - Manage your VAT account - GOV.UK"
      }

      "have the correct error text" in {
        elementText(Selectors.errorText) shouldBe "Error: Select yes if you want to remove the mobile number"
      }

      "has an error summary when there is an error" which {

        "has the correct title text" in {
          elementText(Selectors.errorSummaryTitle) shouldBe "There is a problem"
        }

        "has a link to the error" which {

          "has the correct href" in {
            element(Selectors.errorLink).attr("href") shouldBe "#yes_no-yes"
          }

          "has the correct text" in {
            elementText(Selectors.errorLink) shouldBe "Select yes if you want to remove the mobile number"
          }
        }
      }
    }

  }
}
