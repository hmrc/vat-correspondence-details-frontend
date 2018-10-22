/*
 * Copyright 2018 HM Revenue & Customs
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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class VerifyEmailViewSpec extends ViewBaseSpec {

  val testEmail: String = "test@email.com"
  lazy val view = views.html.verify_email(testEmail)

  lazy implicit val document: Document = Jsoup.parse(view.body)

  "The Verify Email view" should {

    "have the correct heading" in {
      document.getElementsByClass("heading-large").text() shouldBe "Verify your email address"
    }

    "have the correct text for the first paragraph" in {
      elementText("#content p") shouldBe "We’ve sent an email to test@email.com. Click on the link in the email to verify your email address."
    }

    "have the correct text for the second paragraph" in {
      elementText("#content p:nth-of-type(2)") shouldBe "You can change your email address if it is not correct."
    }

    "have a GA tag with the correct tag" in {
      element("#content > article > p:nth-child(3) > a").attr("data-journey-click") shouldBe "email-address:edit:verify-email"
    }

    "have a link element in the first paragraph that links to the Capture your email page" in {
      element("#content > article > p:nth-child(3) > a").attr("href") shouldBe controllers.routes.CaptureEmailController.show().url
    }

    "have an accordion which" should {

      "have a span with the correct text" in {
        elementText("#content span") shouldBe "I did not get the email"
      }

      "have a paragraph with the correct text" in {
        elementText(".panel.panel-border-narrow") shouldBe "Check your junk folder. If it’s not there we can" +
          " send it again. If we send it again, any previous link will stop working."
      }

      "have a GA tag with the correct tag" in {
        element("#content > article > details > div > p > a").attr("data-journey-click") shouldBe "email-address:resend:verify-email"
      }
      
      "have a link element which calls the resend email controller action" in {
        element("#content > article > details > div > p > a").attr("href") shouldBe
          controllers.routes.VerifyEmailController.resendVerification().url
      }
    }
  }
}
