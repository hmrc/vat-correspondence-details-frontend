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

import forms.EmailForm._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html

class CaptureEmailViewSpec extends ViewBaseSpec {

  object Selectors {
    val pageHeading = "#content h1"
    val hintText = "#label-email-hint"
    val form = "form"
    val emailField = "#email"
    val continueButton = "button"
  }

  lazy val view: Html = views.html.capture_email(emailForm)(request, messages, mockConfig)
  lazy implicit val document: Document = Jsoup.parse(view.body)

  "Rendering the capture email page" should {

    s"have the correct document title" in {
      document.title shouldBe "What is the email address?"
    }

    s"have the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe "What is the email address?"
    }

    s"have the correct hint text" in {
      elementText(Selectors.hintText) shouldBe "For example, me@me.com"
    }

    s"have the email form with the correct form action" in {
      element(Selectors.form).attr("action") shouldBe "/vat-through-software/account/correspondence/change-email-address"
    }

    s"have the email text field with the pre-populated value" in {
      element(Selectors.emailField).attr("value") shouldBe ""
    }

    s"have the continue button" in {
      elementText(Selectors.continueButton) shouldBe "Continue"
    }
  }
}
