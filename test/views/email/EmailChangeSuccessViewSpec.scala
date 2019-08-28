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

package views.email

import models.contactPreferences.ContactPreference
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.email.EmailChangeSuccessView

class EmailChangeSuccessViewSpec extends ViewBaseSpec {

  val injectedView: EmailChangeSuccessView = injector.instanceOf[EmailChangeSuccessView]

  object Selectors {
    val title = "title"
    val pageHeading = "h1"
    val secondaryHeading = "h2"
    val paragraphOne = "#content article p:nth-of-type(1)"
    val paragraphTwo = "#content article p:nth-of-type(2)"
    val button = "#content > article > a"
  }

  "The Email Change Successful view" when {

    "the contact preference is successfully retrieved" when {

      "the contact preference is Digital" should {

        lazy val view = injectedView(Some(ContactPreference.digital))(user, messages, mockConfig)

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct page title" in {
          elementText(Selectors.title) shouldBe "We have received the new email address - Business tax account - GOV.UK"
        }

        "have the correct heading" in {
          elementText(Selectors.pageHeading) shouldBe "We have received the new email address"
        }

        "have a GA tag for the page view" in {
          element(Selectors.pageHeading).select("h1").attr("data-journey") shouldBe "email-address:view:change-email-success"
        }

        "have a finish button with the correct text" in {
          elementText(Selectors.button) shouldBe "Finish"
        }

        "have a finish button which navigates to the Change of Circs overview page" in {
          element(Selectors.button).select("a").attr("href") shouldBe "mockManageVatOverviewUrl"
        }

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe "We will send you an email within 2 working days with an update," +
            " followed by a letter to your principal place of business." +
            " You can also go to your HMRC secure messages to find out if your request has been accepted."
        }

        "have the correct second paragraph" in {
          elementText(Selectors.paragraphTwo) shouldBe "Ensure your contact details are up to date."
        }

        "have a GA tag for the clicking finish button" in {
          element(Selectors.button).select("h1").attr("data-journey") contains "email-address:confirm:finish-email-change"
        }

      }

      "the contact preference is Paper" should {

        lazy val view = injectedView(Some(ContactPreference.paper))
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe "We will send a letter to your principal place of" +
            " business with an update within 15 working days."
        }
      }
    }

    "the contact preference is not retrieved" should {

      lazy implicit val document: Document = Jsoup.parse(injectedView().body)

      "have the correct first paragraph" in {
        elementText(Selectors.paragraphOne) shouldBe "We will send you an update within 15 working days."
      }
    }
  }
}
