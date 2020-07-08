/*
 * Copyright 2020 HM Revenue & Customs
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

import assets.LettersToPpobMessages
import forms.YesNoForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.contactPreference.LettersToPpobView

class LettersToPpobSpec extends ViewBaseSpec {

  lazy val lettersToPpobView: LettersToPpobView = injector.instanceOf[LettersToPpobView]
  lazy val address: String = "123 Fake Street, AB1 C23"

  object Selectors {
    val pageHeading = "#content h1"
    val button = ".button"
    val yesOption = "div.multiple-choice:nth-child(1) > label"
    val noOption = "div.multiple-choice:nth-child(2) > label"
    val hint = ".secondary-text"
    val errorHeading = "#error-summary-display"
    val error = ".error-message"
  }

  "Once rendered, the Letters to PPOB view" should {
    lazy val view = lettersToPpobView(YesNoForm.yesNoForm(LettersToPpobMessages.title), address)(
      user, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe LettersToPpobMessages.title
    }

    "have the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe LettersToPpobMessages.heading
    }

    "have the correct hint text" in {
      elementText(Selectors.hint) shouldBe LettersToPpobMessages.hint
    }

    "have the correct continue button text" in {
      elementText(Selectors.button) shouldBe LettersToPpobMessages.continue
    }

    "display the Yes option correctly with an address" in {
      elementText(Selectors.yesOption) shouldBe LettersToPpobMessages.yes
    }

    "display the No option correctly" in {
      elementText(Selectors.noOption) shouldBe LettersToPpobMessages.no
    }

    "not display an error" in {
      document.select(Selectors.error).isEmpty shouldBe true
    }
  }

  "The letters to PPOB view with errors" should {

    lazy val view = lettersToPpobView(YesNoForm.yesNoForm(LettersToPpobMessages.errorMessage)
      .bind(Map("yes_no" -> "")), address)(
      user, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe s"${LettersToPpobMessages.errorTitlePrefix} ${LettersToPpobMessages.title}"
    }

    "have the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe LettersToPpobMessages.heading
    }

    "display the Yes option correctly with an address" in {
      elementText(Selectors.yesOption) shouldBe LettersToPpobMessages.yes
    }

    "display the No option correctly" in {
      elementText(Selectors.noOption) shouldBe LettersToPpobMessages.no
    }

    "have the correct continue button text" in {
      elementText(Selectors.button) shouldBe LettersToPpobMessages.continue
    }

    "display the correct error message" in {
      elementText(Selectors.error) shouldBe LettersToPpobMessages.errorMessage
    }

  }
}
