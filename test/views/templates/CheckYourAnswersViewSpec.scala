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

package views.templates

import models.User
import models.viewModels.CheckYourAnswersViewModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.templates.CheckYourAnswersView

class CheckYourAnswersViewSpec extends ViewBaseSpec {

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
      document.title() shouldBe "Check your answers - Business tax account - GOV.UK"
    }

    "have the correct heading" in {
      elementText("h1") shouldBe "Check your answers"
    }

    "have the correct sub heading" in {
      elementText("h2") shouldBe "VAT business details"
    }

    "contain a row" which {

      "has the correct question text" in {
        elementText(".cya-question") shouldBe "Question"
      }

      "has the correct answer text" in {
        elementText(".cya-answer") shouldBe "Answer"
      }

      "has a change link" which {

        "has the correct text" in {
          elementText(".cya-change") shouldBe "Change"
        }

        "has the correct URL" in {
          element(".cya-change > a").attr("href") shouldBe "/change-link"
        }

        "has hidden text" in {
          element(".cya-change > a").attr("aria-label") shouldBe "Edit the answer"
        }
      }
    }

    "have a continue button" which {

      "has the correct text" in {
        elementText(".button") shouldBe "Continue"
      }

      "has the correct URL" in {
        element(".button").attr("href") shouldBe "/continue-link"
      }
    }
  }
}
