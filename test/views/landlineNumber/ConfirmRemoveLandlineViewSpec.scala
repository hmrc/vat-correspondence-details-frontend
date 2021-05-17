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

package views.landlineNumber

import assets.BaseTestConstants.testValidationLandline
import controllers.landlineNumber.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.landlineNumber.ConfirmRemoveLandlineView

class ConfirmRemoveLandlineViewSpec extends ViewBaseSpec {

  val injectedView: ConfirmRemoveLandlineView = inject[ConfirmRemoveLandlineView]

  "The ConfirmRemoveLandline page" when {

    "the user is a principal entity" should {

      lazy val view = injectedView(testValidationLandline)(user, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct title" in {
        document.title shouldBe
          s"Confirm you want to remove the landline number - Business tax account - GOV.UK"
      }

      "have the correct heading" in {
        elementText("h1") shouldBe s"Confirm you want to remove the landline number: $testValidationLandline"
      }

      "have a form" which {

        "has the correct action" in {
          element("form").attr("action") shouldBe routes.ConfirmRemoveLandlineController.removeLandlineNumber().url
        }

        "has a continue button with the correct text" in {
          elementText(".govuk-button") shouldBe "Confirm and continue"
        }

      }
    }

    "the user is an agent" should {

      lazy val view = injectedView(testValidationLandline)(agent, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct title" in {
        document.title shouldBe
          s"Confirm you want to remove the landline number - Your client’s VAT details - GOV.UK"
      }
    }
  }
}
