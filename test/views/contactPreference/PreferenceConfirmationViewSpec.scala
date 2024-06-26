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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import views.html.contactPreference.PreferenceConfirmationView
import views.ViewBaseSpec

class PreferenceConfirmationViewSpec extends ViewBaseSpec with Matchers {

  val injectedView: PreferenceConfirmationView = inject[PreferenceConfirmationView]

  "The Preference Confirmation page" when {

    "changeType is email" should {

      lazy val view = injectedView(Seq("pepsi-mac@test.com"), "vatCorrespondenceLetterToEmailChangeSuccessful")(getRequest, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct title" in {
        document.title shouldBe "You have asked us to change how we contact you about VAT - VAT - GOV.UK"
      }

      "have the correct heading" in {
        elementText("h1") shouldBe "You have asked us to change how we contact you about VAT"
      }

      "have the correct subheading" in {
        elementText("h2") shouldBe "What happens next"
      }

      "have the correct first paragraph" in {
        elementText("#content > p:nth-child(3)") shouldBe
          "If we can accept the change, we will contact you about VAT at:"
      }

      "have the correct email address" in {
        elementText("#content > div.govuk-inset-text") shouldBe
          "pepsi-mac@test.com"
      }

      "have the correct second paragraph" in {
        elementText("#content > p:nth-child(5)") shouldBe
          "We’ll still need to send you some letters in the post because the law tells us to."
      }

      "have a button" which {

        "has the correct text" in {
          elementText(".govuk-button") shouldBe "View your account details"
        }

        "has the correct href" in {
          element(".govuk-button").attr("href") shouldBe mockConfig.btaAccountDetailsUrl
        }
      }
    }

    "changeType is letter" should {

      lazy val view = injectedView(Seq("Address line 1", "Address line 2", "Address line 3"),
        "vatCorrespondenceEmailToLetterChangeSuccessful")(getRequest, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "render all lines of address" in {

        elementText("#content > div.govuk-inset-text") shouldBe
          "Address line 1 Address line 2 Address line 3"

      }

      "not render section regarding sending letters" in {
        elementExtinct("#content > p:nth-child(5)")
      }
    }
  }
}
