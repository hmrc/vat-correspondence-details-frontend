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

import forms.EmailForm._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.html.CaptureEmailView

class CaptureEmailViewSpec extends ViewBaseSpec {

  val injectedView: CaptureEmailView = injector.instanceOf[CaptureEmailView]

  object Selectors {
    val pageHeading = "#content h1"
    val backLink = "#content > article > a"
    val hintText = "#label-email-hint"
    val form = "form"
    val emailField = "#email"
    val continueButton = "button"
    val errorSummary = "#error-summary-heading"
    val emailFormGroup = "#content > article > form > div:nth-child(1)"
  }

  "Rendering the capture email page" when {

    "the form has no errors" should {
      lazy val view: Html = injectedView(emailForm(testEmail).fill(testEmail), emailNotChangedError = false)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct document title" in {
        document.title shouldBe "What is the email address?"
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
        elementText(Selectors.pageHeading) shouldBe "What is the email address?"
      }

      "have the correct hint text" in {
        elementText(Selectors.hintText) shouldBe "For example, me@me.com"
      }

      "have the email form with the correct form action" in {
        element(Selectors.form).attr("action") shouldBe "/vat-through-software/account/correspondence/change-email-address"
      }

      "have the email text field with the pre-populated value" in {
        element(Selectors.emailField).attr("value") shouldBe "test@email.co.uk"
      }

      "have the continue button" in {
        elementText(Selectors.continueButton) shouldBe "Continue"
      }
    }

    "the form has the email unchanged error" should {
      lazy val view = injectedView(emailForm("").bind(Map("email" -> "")), emailNotChangedError = true)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the error summary" in {
        element(Selectors.errorSummary).text() shouldBe "There is a problem"
      }

      "display the GA tag" in {
        element(Selectors.emailFormGroup).attr("data-journey") shouldBe "email-address:form-error:unchanged"
      }
    }

    "the form has any other error" should {
      lazy val view = injectedView(emailForm("").bind(Map("email" -> "invalid")), emailNotChangedError = false)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "display the error summary" in {
        element(Selectors.errorSummary).text() shouldBe "There is a problem"
      }

      "display no GA tag" in {
        element(Selectors.emailFormGroup).attr("data-journey") shouldBe empty
      }
    }
  }
}
