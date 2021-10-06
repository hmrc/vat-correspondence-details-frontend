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

package views.templates

import models.User
import models.viewModels.CheckYourAnswersViewModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import views.ViewBaseSpec
import views.html.templates.CheckYourAnswersView

class CheckYourAnswersViewSpec extends ViewBaseSpec with Matchers {

  "CheckYourAnswersView" should {

    val viewModel = CheckYourAnswersViewModel(
      "Question",
      "Answer",
      "/change-link",
      "Edit the answer",
      "/continue-link"
    )

    val injectedView: CheckYourAnswersView = inject[CheckYourAnswersView]
    lazy val view = injectedView(viewModel)(messages, mockConfig, User("1111111111"))
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct page title" in {
      document.title() shouldBe "Check your answers - Manage your VAT account - GOV.UK"
    }

    "have the correct heading" in {
      elementText("h1") shouldBe "Check your answers"
    }

    "have the correct sub heading" in {
      elementText("h2") shouldBe "VAT business details"
    }

    "contain a row" which {

      "has the correct question text" in {
        elementText(".govuk-summary-list__key") shouldBe "Question"
      }

      "has the correct answer text" in {
        elementText(".govuk-summary-list__value") shouldBe "Answer"
      }

      "has a change link" which {

        "has the correct text" in {
          elementText(".govuk-summary-list__actions > a > span:nth-child(1)") shouldBe "Change"
        }

        "has the correct URL" in {
          element(".govuk-summary-list__actions > a").attr("href") shouldBe "/change-link"
        }

        "has hidden text" in {
          elementText(".govuk-summary-list__actions > a > span:nth-child(2)") shouldBe "Edit the answer"
        }
      }
    }

    "have a continue button" which {

      "has the correct text" in {
        elementText(".govuk-button") shouldBe "Continue"
      }

      "has the correct URL" in {
        element(".govuk-button").attr("href") shouldBe "/continue-link"
      }
    }
  }
}
