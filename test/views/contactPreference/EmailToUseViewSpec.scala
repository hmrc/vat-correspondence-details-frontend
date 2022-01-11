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

package views.contactPreference

import assets.{ChangePrefMessages => viewMessages}
import forms.YesNoForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import play.twirl.api.Html
import views.ViewBaseSpec
import views.html.contactPreference.EmailToUseView

class EmailToUseViewSpec extends ViewBaseSpec with Matchers {

  val injectedView: EmailToUseView = injector.instanceOf[EmailToUseView]

  private object Selectors {
    val pageHeading = "#content h1"
    val email = ".govuk-inset-text"
    val yesOption = ".govuk-radios > div:nth-child(1) > label"
    val noOption = ".govuk-radios > div:nth-child(2) > label"
    val button = ".govuk-button"
    val errorHeading = ".govuk-error-summary"
    val error = "#yes_no-error"
    val backLink = ".govuk-back-link"
  }

  "The EmailToUseView" should {

    lazy val view: Html = injectedView(YesNoForm.yesNoForm(""), "email@Address.com")(user, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe viewMessages.title
    }

    "display the correct heading" in {
      elementText(Selectors.pageHeading) shouldBe viewMessages.heading
    }

    "display the correct email address" in {
      elementText(Selectors.email) shouldBe "email@Address.com"
    }

    "have the correct radio buttons with yes/no answers" in {
      elementText(Selectors.yesOption) shouldBe viewMessages.yes
      elementText(Selectors.noOption) shouldBe viewMessages.no
    }

    "have the correct continue button text" in {
      elementText(Selectors.button) shouldBe viewMessages.continue
    }

    "not display an error" in {
      document.select(Selectors.error).isEmpty shouldBe true
    }
    "have the correct back link" which {
      "should have the correct text" in {
        elementText(Selectors.backLink) shouldBe "Back"
      }

      "should have the correct href" in {
        element(Selectors.backLink).attr("href") shouldBe "/vat-through-software/account/correspondence/contact-preference-email"
      }
    }
  }

  "The EmailToUseView with errors" should {

    lazy val view = injectedView(YesNoForm.yesNoForm(viewMessages.errorMessage).bind(Map("yes_no" -> "")), "email@Address.com")(user, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe s"${viewMessages.errorTitlePrefix} ${viewMessages.title}"
    }

    "have the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe viewMessages.heading
    }

    "display the correct email address" in {
      elementText(Selectors.email) shouldBe "email@Address.com"
    }

    "display the correct error heading" in {
      elementText(Selectors.errorHeading) shouldBe s"${viewMessages.errorHeading} ${viewMessages.errorMessage}"
    }

    "have the correct radio buttons with yes/no answers" in {
      elementText(Selectors.yesOption) shouldBe viewMessages.yes
      elementText(Selectors.noOption) shouldBe viewMessages.no
    }

    "have the correct continue button text" in {
      elementText(Selectors.button) shouldBe viewMessages.continue
    }

    "display the correct error message" in {
      elementText(Selectors.error) shouldBe s"${viewMessages.errorTitlePrefix} ${viewMessages.errorMessage}"
    }
  }

}
