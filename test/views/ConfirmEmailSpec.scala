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

class ConfirmEmailSpec extends ViewBaseSpec {

  val testEmail: String = "test@email.com"
  lazy val view = views.html.confirm_email(testEmail)

  lazy implicit val document: Document = Jsoup.parse(view.body)

  "The Confirm Email view" should {
    "have the correct heading" in {
      document.getElementsByClass("heading-large").text() shouldBe "Confirm the new email address"
    }

    "have the email address the user provided" in {
      document.text() contains testEmail
    }

    "have a continue button" in {
      document.getElementById("continue").text() shouldBe "Confirm and continue"
    }

    //TODO
    "have a link to edit email address" in {
    }

    "have some body text" in {
      document.text() contains "By confirming this change, you agree that the information you have given is complete and correct."
    }
  }
}
