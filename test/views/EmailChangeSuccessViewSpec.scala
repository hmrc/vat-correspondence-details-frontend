/*
 * Copyright 2019 HM Revenue & Customs
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

class EmailChangeSuccessViewSpec extends ViewBaseSpec {

  "The Email Change Successful view" should {

    lazy val view = views.html.email_change_success()
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct heading" in {
      document.getElementsByClass("heading-xlarge").text() shouldBe "We have received the new email address"
    }

    "have a finish button with the correct text" in {
      document.getElementsByClass("button").text() shouldBe "Finish"
    }

    "have a finish button which navigates to the Change of Circs overview page" in {
      document.getElementsByClass("button").attr("href") shouldBe "mockManageVatOverviewUrl"
    }

    "have some body text" in {
      document.text() contains "We will send an email within 2 working days " +
        "telling you whether or not the request has been accepted. " +
        "You can also go to your messages in your business tax account." +
        "\n\nEnsure your contact details are up to date."
    }

    "have a GA tag for the page view" in {
      document.text() contains "data-journey=\"email-address:view:change-email-success\""
    }

    "have a GA tag for the clicking finish button" in {
      document.text() contains "data-journey-click=\"email-address:confirm:finish-email-change\""
    }
  }
}
