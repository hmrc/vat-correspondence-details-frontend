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

package views.contactPreference

import assets.LetterPreferenceMessages
import forms.YesNoForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.contactPreference.LetterPreferenceView

class LetterPreferenceViewSpec extends ViewBaseSpec {

  lazy val letterPreferenceView: LetterPreferenceView = injector.instanceOf[LetterPreferenceView]
  lazy val address: String = "123 Fake Street, AB1 C23"

  object Selectors {
    val pageHeading = "#content h1"
    val button = ".button"
    val yesOption = "div.multiple-choice:nth-child(1) > label"
    val noOption = "div.multiple-choice:nth-child(2) > label"
    val hint = ".panel"
    val errorHeading = "#error-summary-display h2"
    val errorSummaryMessage = "#yes_no-error-summary"
    val error = ".error-message"
    val backLink = "#content > article > a"
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

    "have the correct continue button text" in {
      elementText(Selectors.button) shouldBe LetterPreferenceMessages.continue
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
