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

class EmailChangeSuccessfulSpec extends ViewBaseSpec {

  val testEmail: String = "test@email.com"
  lazy val view = views.html.email_change_successful()

  lazy implicit val document: Document = Jsoup.parse(view.body)

  "The Email Change Successful view" should {
    "have the correct heading" in {
      document.getElementsByClass("heading-large").text() shouldBe "We have received the new email address"
    }

    "have a finish button with the correct text" in {
      document.getElementById("finish").text() shouldBe "Finish"
    }

    "have a finish button which navigates to vat overview" in {
      document.getElementById("finish").attr("href") shouldBe "/vat-through-software/account/change-business-details"
    }

    "have some body text" in {
      document.text() contains "We will send an email within 2 working days " +
        "telling you whether or not the request has been accepted. " +
        "You can also go to your messages in your business tax account." +
        "\n\nEnsure your contact details are up to date."
    }
  }
}
