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

package views.email

import views.html.email.PasscodeErrorView
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.ViewBaseSpec

class PasscodeErrorViewSpec extends ViewBaseSpec {

  val injectedView: PasscodeErrorView = inject[PasscodeErrorView]
  lazy val view: Html = injectedView("passcode.error.tooManyAttempts")(user, messages, mockConfig)

  lazy implicit val document: Document = Jsoup.parse(view.body)

  "The passcode error view" should {


    "have the correct document title" in {
      document.title shouldBe "You need to start again - Business tax account - GOV.UK"
    }

    "have the correct heading" in {
      elementText(".govuk-heading-l") shouldBe "You need to start again"
    }

    "have the correct text for the first paragraph" in {
      elementText("#content p:nth-child(2)") shouldBe "This is because you have entered the wrong code too many times."
    }

    "have the correct text for the second paragraph" in {
      elementText("#content p:nth-child(3)") shouldBe "Return to your VAT account" + " to start the process again."
    }

    "have a link that takes you to VAT account" which {

      "has the correct text" in {
        elementText("#content p:nth-child(3) > a") shouldBe "Return to your VAT account"
      }

      "have the correct href" in {
        element("#content p:nth-child(3) > a").attr("href") shouldBe
          mockConfig.vatOverviewUrl
      }
    }
  }
}