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

package views.website

import models.viewModels.WebsiteChangeSuccessViewModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.website.WebsiteChangeSuccessView

class WebsiteChangeSuccessViewSpec extends ViewBaseSpec {

  lazy val injectedView: WebsiteChangeSuccessView = injector.instanceOf[WebsiteChangeSuccessView]

  "The WebsiteChangeSuccess page" when {

    "the user is a principal entity" when {

      "the user has requested to change their website address and has a digital preference" should {

        val model = WebsiteChangeSuccessViewModel(None, Some("DIGITAL"), removeWebsite = false, None)
        lazy val view = injectedView(model)(request, messages, mockConfig, user)
        implicit lazy val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe "Request to change website address received"
        }

        "have the correct heading" in {
          elementText("h1") shouldBe "Request to change website address received"
        }

        "have the correct subheading" in {
          elementText("h2") shouldBe "What happens next"
        }

        "have the correct first paragraph" in {
          elementText("article > p:nth-of-type(1)") shouldBe
            "We‘ll send you an email within 2 working days with an update, followed by a letter to your principal " +
              "place of business. You can also check your HMRC secure messages for an update."
        }

        "have the correct second paragraph" in {
          elementText("article > p:nth-of-type(2)") shouldBe "Make sure your contact details are up to date."
        }

        "have a button" which {

          "has the correct text" in {
            elementText(".button") shouldBe "Finish"
          }

          "has the correct link location" in {
            element(".button").attr("href") shouldBe mockConfig.manageVatSubscriptionServicePath
          }
        }
      }

      "the user has requested to remove their website address and has a paper preference" should {

        val model = WebsiteChangeSuccessViewModel(None, Some("PAPER"), removeWebsite = true, None)
        lazy val view = injectedView(model)(request, messages, mockConfig, user)
        implicit lazy val document: Document = Jsoup.parse(view.body)

        "have the correct title" in {
          document.title shouldBe "Request to remove website address received"
        }

        "have the correct heading" in {
          elementText("h1") shouldBe "Request to remove website address received"
        }

        "have the correct first paragraph" in {
          elementText("article > p:nth-of-type(1)") shouldBe
            "We will send a letter to your principal place of business with an update within 15 working days."
        }
      }

      "the call to get the user's preference fails" should {

        val model = WebsiteChangeSuccessViewModel(None, None, removeWebsite = false, None)
        lazy val view = injectedView(model)(request, messages, mockConfig, user)
        implicit lazy val document: Document = Jsoup.parse(view.body)

        "have the correct first paragraph" in {
          elementText("article > p:nth-of-type(1)") shouldBe "We will send you an update within 15 working days."
        }
      }
    }

    "the user is an agent" when {

      "the user has requested to receive notifications and a client business name was retrieved" should {

        val model = WebsiteChangeSuccessViewModel(Some("Pepsi Mac"), None, removeWebsite = false, Some("agent@test.com"))
        lazy val view = injectedView(model)(request, messages, mockConfig, agent)
        implicit lazy val document: Document = Jsoup.parse(view.body)

        "have the correct first paragraph" in {
          elementText("article > p:nth-of-type(1)") shouldBe "We‘ll send an email to agent@test.com within 2 " +
            "working days telling you whether or not the request has been accepted."
        }

        "have the correct second paragraph" in {
          elementText("article > p:nth-of-type(2)") shouldBe "We‘ll also contact Pepsi Mac with an update."
        }

        "have a link to change client" which {

          "has the correct text" in {
            elementText("article > p:nth-of-type(3) > a") shouldBe "Change client"
          }

          "has the correct link location" in {
            element("article > p:nth-of-type(3) > a").attr("href") shouldBe mockConfig.vatAgentClientLookupServicePath
          }
        }
      }

      "the user has requested not to receive notifications and a business name was not retrieved" should {

        val model = WebsiteChangeSuccessViewModel(None, None, removeWebsite = false, None)
        lazy val view = injectedView(model)(request, messages, mockConfig, agent)
        implicit lazy val document: Document = Jsoup.parse(view.body)

        "have the correct first paragraph" in {
          elementText("article > p:nth-of-type(1)") shouldBe
            "We‘ll send a confirmation letter to the agency address registered with HMRC within 15 working days."
        }

        "have the correct second paragraph" in {
          elementText("article > p:nth-of-type(2)") shouldBe "We‘ll also contact your client with an update."
        }
      }
    }
  }
}
