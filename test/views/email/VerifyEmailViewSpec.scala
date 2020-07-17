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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.ViewBaseSpec
import views.html.email.VerifyEmailView

class VerifyEmailViewSpec extends ViewBaseSpec {

  val injectedView: VerifyEmailView = injector.instanceOf[VerifyEmailView]

  "The Verify Email view without contact pref change" should {

    lazy val view: Html = injectedView(testEmail, isContactPrefJourney = false)(user, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe "Confirm your email address - Business tax account - GOV.UK"
    }

    "have the correct heading" in {
      document.getElementsByClass("heading-large").text() shouldBe "Confirm your email address"
    }

    "have the correct text for the first paragraph" in {
      elementText("#content article p:nth-of-type(1)") shouldBe
        "We’ve sent an email to test@email.co.uk"
    }

    "have the correct text for the second paragraph" in {
      elementText("#content article p:nth-of-type(2)") shouldBe
        "You need to click the link in our email within 15 minutes. This will confirm your email address."
    }

    "have the correct text for the third paragraph" in {
      elementText("#content article p:nth-of-type(3)") shouldBe "Check your junk folder. If it’s not there we can send it again."
    }

    "have a link" which {

      "has the correct link text" in {
        elementText("#content > article > p:nth-of-type(3) > a") shouldBe "send it again"
      }

      "has the correct href" in {
        element("#content > article > p:nth-of-type(3) > a").attr("href") shouldBe
          controllers.email.routes.VerifyEmailController.emailSendVerification().url
      }
    }
  }

  "The Verify Email view with contact pref change" should {

    lazy val view: Html = injectedView(testEmail, isContactPrefJourney = true)(user, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have a link" which {

      "has the correct href" in {
        element("#content > article > p:nth-of-type(3) > a").attr("href") shouldBe
          controllers.email.routes.VerifyEmailController.contactPrefSendVerification().url
      }
    }
  }
}
