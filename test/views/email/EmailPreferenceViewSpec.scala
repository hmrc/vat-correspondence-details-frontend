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

package views.email

import forms.YesNoForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.email.EmailPreferenceView

class EmailPreferenceViewSpec extends ViewBaseSpec {
  lazy val emailPrefView: EmailPreferenceView = injector.instanceOf[EmailPreferenceView]

  object Selectors {
    val pageHeading = "#content h1"
    val button = ".button"
    val yesOption = "div.multiple-choice:nth-child(1) > label"
    val noOption = "div.multiple-choice:nth-child(2) > label"
  }

  "Once rendered, the email preference page" should {
    lazy val view = emailPrefView(YesNoForm.yesNoForm("emailPrefView.title"))(user, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe messages("emailPreference.title") + " - Business tax account - GOV.UK"
    }

    "have the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe messages("emailPreference.title")
    }

    "have the correct continue button text" in {
      elementText(Selectors.button) shouldBe messages("common.continue")
    }

    "have the correct radio buttons with yes/no answers" in {
      elementText(Selectors.yesOption) shouldBe messages("common.yes")
      elementText(Selectors.noOption) shouldBe messages("common.no")
    }
  }
}
