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

package views.email

import forms.BouncedEmailForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import play.api.data.Form
import views.ViewBaseSpec
import views.html.email.BouncedEmailView

class BouncedEmailViewSpec extends ViewBaseSpec with Matchers {

  val bouncedEmailView: BouncedEmailView = injector.instanceOf[BouncedEmailView]

  "Rendering the bounced email page with no errors" should {

    val form: Form[Option[String]] = BouncedEmailForm.bouncedEmailForm

    lazy val view = bouncedEmailView(form, "123@abc.com")(request, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct page title" in {
      document.title shouldBe "Choose how to fix your email address - VAT - GOV.UK"
    }

    "have the correct page heading" in {
      elementText("h1") shouldBe "Choose how to fix your email address"
    }

    "not display an error" in {
      document.select("#error-summary-display").isEmpty shouldBe true
    }

    "have the correct form hint" in {
      elementText(".govuk-hint") shouldBe "You can either verify your current email or add a new one."
    }

    "have the correct radio button options" in {
      elementText(".govuk-radios > div:nth-child(1) > label") shouldBe "Verify 123@abc.com"
      elementText(".govuk-radios > div:nth-child(2) > label") shouldBe "Add a new email address"
    }

    "have the correct hint text for the radio button options" in {
      elementText("#verify-add-item-hint") shouldBe
        "We’ll send a confirmation code to this email address. You can enter it on the next screen."
      elementText("#verify-add-2-item-hint") shouldBe
        "This will replace 123@abc.com as your contact email address for your VAT account."
    }
  }

  "Rendering the bounced email page with errors" should {

    val form: Form[Option[String]] = BouncedEmailForm.bouncedEmailForm.bind(Map("verifyAdd" -> ""))

    lazy val view = bouncedEmailView(form, "123@abc.com")(request, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct error document title" in {
      document.title shouldBe "Error: Choose how to fix your email address - VAT - GOV.UK"
    }

    "have the correct page heading" in {
      elementText("h1") shouldBe "Choose how to fix your email address"
    }

    "should display an error" in {
      elementText(".govuk-error-summary") shouldBe "There is a problem Choose an option"
    }

    "have the correct radio button options" in {
      elementText(".govuk-radios > div:nth-child(1) > label") shouldBe "Verify 123@abc.com"
      elementText(".govuk-radios > div:nth-child(2) > label") shouldBe "Add a new email address"
    }

    "have the correct hint text for the radio button options" in {
      elementText("#verify-add-item-hint") shouldBe
        "We’ll send a confirmation code to this email address. You can enter it on the next screen."
      elementText("#verify-add-2-item-hint") shouldBe
        "This will replace 123@abc.com as your contact email address for your VAT account."
    }
  }

}
