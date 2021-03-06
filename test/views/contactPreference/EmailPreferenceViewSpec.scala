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

import assets.EmailPrefMessages
import forms.YesNoForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.contactPreference.EmailPreferenceView

class EmailPreferenceViewSpec extends ViewBaseSpec {
  lazy val emailPrefView: EmailPreferenceView = injector.instanceOf[EmailPreferenceView]

  object Selectors {
    val pageHeading = "#content h1"
    val button = ".button"
    val yesOption = "div.multiple-choice:nth-child(1) > label"
    val noOption = "div.multiple-choice:nth-child(2) > label"
    val hint = ".panel"
    val errorHeading = "#error-summary-display"
    val error = ".error-message"
    val backLink = "#content > article > a"
  }

  "Once rendered, the email preference page" should {
    lazy val view = emailPrefView(YesNoForm.yesNoForm(EmailPrefMessages.title))(user, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe EmailPrefMessages.title
    }

    "have the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe EmailPrefMessages.heading
    }

    "have the correct hint text" in {
      elementText(Selectors.hint) shouldBe EmailPrefMessages.hint
    }

    "have the correct continue button text" in {
      elementText(Selectors.button) shouldBe EmailPrefMessages.continue
    }

    "have the correct radio buttons with yes/no answers" in {
      elementText(Selectors.yesOption) shouldBe EmailPrefMessages.yes
      elementText(Selectors.noOption) shouldBe EmailPrefMessages.no
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

  "The email preference page with errors" should {
    lazy val view = emailPrefView(YesNoForm.yesNoForm(EmailPrefMessages.errorMessage).bind(Map("yes_no" -> "")))(
      user, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe s"${EmailPrefMessages.errorTitlePrefix} ${EmailPrefMessages.title}"
    }

    "have the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe EmailPrefMessages.heading
    }

    "display the correct error heading" in {
      elementText(Selectors.errorHeading) shouldBe s"${EmailPrefMessages.errorHeading} ${EmailPrefMessages.errorMessage}"
    }

    "have the correct radio buttons with yes/no answers" in {
      elementText(Selectors.yesOption) shouldBe EmailPrefMessages.yes
      elementText(Selectors.noOption) shouldBe EmailPrefMessages.no
    }

    "have the correct continue button text" in {
      elementText(Selectors.button) shouldBe EmailPrefMessages.continue
    }

    "display the correct error message" in {
      elementText(Selectors.error) shouldBe s"${EmailPrefMessages.errorTitlePrefix} ${EmailPrefMessages.errorMessage}"
    }
  }
}
