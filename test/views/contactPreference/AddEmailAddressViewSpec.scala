/*
 * Copyright 2024 HM Revenue & Customs
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

package views.contactPreference

import forms.YesNoForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.contactPreference.AddEmailAddressView
import assets.ContactPrefAddEmailMessages
import controllers.contactPreference.routes
import org.scalatest.matchers.should.Matchers

class AddEmailAddressViewSpec extends ViewBaseSpec with Matchers {
  lazy val addEmailView: AddEmailAddressView = injector.instanceOf[AddEmailAddressView]

  object Selectors {
    val pageHeading = "#content h1"
    val backLink = ".govuk-back-link"
    val form = "form"
    val button = ".govuk-button"
    val line1 = "#content p:nth-of-type(1)"
    val line2 = "#content p:nth-of-type(2)"
    val question = "#content > form > div > fieldset > legend"
    val yesOption = "div.govuk-radios__item:nth-child(1) > label:nth-child(2)"
    val noOption = "div.govuk-radios__item:nth-child(2) > label:nth-child(2)"
    val errorHeading = ".govuk-error-summary h2"
    val error = ".govuk-error-message"
  }

  "Once rendered, the add email address page" should {

    lazy val view = addEmailView(YesNoForm.yesNoForm(ContactPrefAddEmailMessages.title))(user, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe ContactPrefAddEmailMessages.title
    }

    "have the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe ContactPrefAddEmailMessages.heading
    }

    "have the correct paragraph content" in {
      elementText(Selectors.line1) shouldBe ContactPrefAddEmailMessages.info1
      elementText(Selectors.line2) shouldBe ContactPrefAddEmailMessages.info2
    }

    "have the correct question" in {
      elementText(Selectors.question) shouldBe ContactPrefAddEmailMessages.question
    }

    "have a back link" which {

      "should have the correct text" in {
        elementText(Selectors.backLink) shouldBe "Back"
      }

      "should have the correct href" in {
        element(Selectors.backLink).attr("href") shouldBe routes.EmailPreferenceController.show.url
      }
    }

    "have the correct continue button text" in {
      elementText(Selectors.button) shouldBe ContactPrefAddEmailMessages.continue
    }

    "have the correct radio buttons with yes/no answers" in {
      elementText(Selectors.yesOption) shouldBe ContactPrefAddEmailMessages.yes
      elementText(Selectors.noOption) shouldBe ContactPrefAddEmailMessages.no
    }

    "not display an error" in {
      document.select(Selectors.error).isEmpty shouldBe true
    }

    "have the correct submit action URL" in {
      element(Selectors.form).attr("action") shouldBe controllers.contactPreference.routes.AddEmailAddressController.submit.url
    }
  }

  "The add email address page with errors" should {
    lazy val view = addEmailView(YesNoForm.yesNoForm(ContactPrefAddEmailMessages.errorMessage).bind(Map("yes_no" -> "")))(
      user, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe s"${ContactPrefAddEmailMessages.errorTitlePrefix} ${ContactPrefAddEmailMessages.title}"
    }

    "have the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe ContactPrefAddEmailMessages.heading
    }

    "display the correct error heading" in {
      elementText(Selectors.errorHeading) shouldBe s"${ContactPrefAddEmailMessages.errorHeading}"
    }

    "display the correct error message" in {
      elementText(Selectors.error) shouldBe s"${ContactPrefAddEmailMessages.errorTitlePrefix} ${ContactPrefAddEmailMessages.errorMessage}"
    }
  }
}
