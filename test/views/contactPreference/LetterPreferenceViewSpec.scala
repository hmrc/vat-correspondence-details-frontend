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

import assets.LetterPreferenceMessages
import forms.YesNoForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import views.ViewBaseSpec
import views.html.contactPreference.LetterPreferenceView

class LetterPreferenceViewSpec extends ViewBaseSpec with Matchers {

  lazy val letterPreferenceView: LetterPreferenceView = injector.instanceOf[LetterPreferenceView]
  lazy val address: String = "123 Fake Street, AB1 C23"

  object Selectors {
    val pageHeading = "h1"
    val button = ".govuk-button"
    val yesOption = "label[for=yes_no]"
    val noOption = "label[for=yes_no-2]"
    val hint = ".govuk-inset-text"
    val errorHeading = ".govuk-error-summary__title"
    val errorSummaryMessage = ".govuk-error-summary__list > li > a"
    val error = ".govuk-error-message"
    val backLink = ".govuk-back-link"
  }

  "Once rendered, the Letters to PPOB view" should {
    lazy val view = letterPreferenceView(YesNoForm.yesNoForm(LetterPreferenceMessages.title), address)(
      user, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe LetterPreferenceMessages.title
    }

    "have the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe LetterPreferenceMessages.heading
    }

    "have the correct hint text" in {
      elementText(Selectors.hint) shouldBe LetterPreferenceMessages.hint
    }

    "have a form with the correct action" in {
      element("form").attr("action") shouldBe "/vat-through-software/account/correspondence/contact-preference-letter"
    }

    "have continue button" which {

      "has the correct button text" in {
        elementText(Selectors.button) shouldBe LetterPreferenceMessages.continue
      }

      "has the prevent double click attribute" in {
        element(Selectors.button).hasAttr("data-prevent-double-click") shouldBe true
      }
    }

    "display the Yes option correctly with an address" in {
      elementText(Selectors.yesOption) shouldBe LetterPreferenceMessages.yes
    }

    "display the No option correctly" in {
      elementText(Selectors.noOption) shouldBe LetterPreferenceMessages.no
    }

    "not display an error" in {
      document.select(Selectors.error).isEmpty shouldBe true
    }
    "have the correct back link" which {
      "should have the correct text" in {
        elementText(Selectors.backLink) shouldBe "Back"
      }

      "should have the correct href" in {
        element(Selectors.backLink).attr("href") shouldBe mockConfig.btaAccountDetailsUrl
      }
    }
  }

  "The letters to PPOB view with errors" should {

    lazy val view = letterPreferenceView(YesNoForm.yesNoForm(LetterPreferenceMessages.errorMessage)
      .bind(Map("yes_no" -> "")), address)(
      user, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe s"${LetterPreferenceMessages.errorTitlePrefix} ${LetterPreferenceMessages.title}"
    }

    "have the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe LetterPreferenceMessages.heading
    }

    "display an error summary" which {

      "has the correct title text" in {
        elementText(Selectors.errorHeading) shouldBe "There is a problem"
      }

      "has an error" in {
        elementText(Selectors.errorSummaryMessage) shouldBe LetterPreferenceMessages.errorMessage
      }
    }

    "display the Yes option correctly with an address" in {
      elementText(Selectors.yesOption) shouldBe LetterPreferenceMessages.yes
    }

    "display the No option correctly" in {
      elementText(Selectors.noOption) shouldBe LetterPreferenceMessages.no
    }

    "have the correct continue button text" in {
      elementText(Selectors.button) shouldBe LetterPreferenceMessages.continue
    }

    "display the correct error message" in {
      elementText(Selectors.error) shouldBe s"${LetterPreferenceMessages.errorTitlePrefix} ${LetterPreferenceMessages.errorMessage}"
    }

  }
}
