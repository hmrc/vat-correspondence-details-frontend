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

package views.templates

import assets.BaseTestConstants
import models.User
import models.contactPreferences.ContactPreference
import models.viewModels.ChangeSuccessViewModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.templates.ChangeSuccessView

class ChangeSuccessViewSpec extends ViewBaseSpec {

  val injectedView: ChangeSuccessView = inject[ChangeSuccessView]

  object Selectors {
    val title = "title"
    val pageHeading = "h1"
    val secondaryHeading = "h2"
    val paragraphOne = "#content article p:nth-of-type(1)"
    val paragraphTwo = "#content article p:nth-of-type(2)"
    val button = "#content > article > a"
  }

  val exampleTitle = "ExampleTitle"

  "The Change Successful view" when {

    "an individual is performing the action" when {

      "the contact preference is successfully retrieved" when {

        "the contact preference is Digital" should {

          val viewModel = ChangeSuccessViewModel(exampleTitle, None, Some(ContactPreference.digital), None)
          lazy val view = injectedView(viewModel)(request, messages, mockConfig, User("1111111111"))

          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the page title provided by the model" in {
            elementText(Selectors.title) shouldBe s"$exampleTitle - Business tax account - GOV.UK"
          }

          "have the heading provided by the model" in {
            elementText(Selectors.pageHeading) shouldBe exampleTitle
          }

          "have a finish button with the correct text" in {
            elementText(Selectors.button) shouldBe "Finish"
          }

          "have a finish button which navigates to the Change of Circs overview page" in {
            element(Selectors.button).select("a").attr("href") shouldBe mockConfig.manageVatSubscriptionServicePath
          }

          "have the correct first paragraph" in {
            elementText(Selectors.paragraphOne) shouldBe "We’ll send you an email within 2 working days with an update," +
              " followed by a letter to your principal place of business." +
              " You can also check your HMRC secure messages for an update."
          }

          "have the correct second paragraph" in {
            elementText(Selectors.paragraphTwo) shouldBe "Make sure your contact details are up to date."
          }
        }

        "the contact preference is Paper" should {
          val viewModel = ChangeSuccessViewModel(exampleTitle, None, Some(ContactPreference.paper), None)
          lazy val view = injectedView(viewModel)(request, messages, mockConfig, User("1111111111"))
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the correct first paragraph" in {
            elementText(Selectors.paragraphOne) shouldBe "We’ll send a letter to your principal place of" +
              " business with an update within 15 working days."
          }
        }
      }

      "the contact preference is not retrieved" should {
        val viewModel = ChangeSuccessViewModel(exampleTitle, None, None, None)
        lazy implicit val document: Document = Jsoup.parse(injectedView(viewModel)(request, messages, mockConfig, User("1111111111")).body)

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe "We will send you an update within 15 working days."
        }
      }
    }

    "an agent is performing the action" when {

      "the agent has an email address registered" should {
        val viewModel = ChangeSuccessViewModel(exampleTitle, Some("agent@example.com"), None, Some("TheBusiness"))
        lazy val view = injectedView(viewModel)(request, messages, mockConfig, User("1111111111", arn = Some(BaseTestConstants.arn)))

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the page title provided by the model" in {
          elementText(Selectors.title) shouldBe s"$exampleTitle - Your client’s VAT details - GOV.UK"
        }

        "have the heading provided by the model" in {
          elementText(Selectors.pageHeading) shouldBe exampleTitle
        }

        "have a finish button with the correct text" in {
          elementText(Selectors.button) shouldBe "Finish"
        }

        "have a finish button which navigates to the Change of Circs overview page" in {
          element(Selectors.button).select("a").attr("href") shouldBe mockConfig.manageVatSubscriptionServicePath
        }

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe "We’ll send an email to agent@example.com within 2" +
            " working days telling you whether or not the request has been accepted."
        }

        "have the correct second paragraph" in {
          elementText(Selectors.paragraphTwo) shouldBe "We’ll also contact TheBusiness with an update."
        }
      }

      "the agent doesn't have an email address registered" should {
        val viewModel = ChangeSuccessViewModel(exampleTitle, None, None, Some("TheBusiness"))
        lazy val view = injectedView(viewModel)(request, messages, mockConfig, User("1111111111", arn = Some(BaseTestConstants.arn)))

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the page title provided by the model" in {
          elementText(Selectors.title) shouldBe s"$exampleTitle - Your client’s VAT details - GOV.UK"
        }

        "have the heading provided by the model" in {
          elementText(Selectors.pageHeading) shouldBe exampleTitle
        }

        "have a finish button with the correct text" in {
          elementText(Selectors.button) shouldBe "Finish"
        }

        "have a finish button which navigates to the Change of Circs overview page" in {
          element(Selectors.button).select("a").attr("href") shouldBe mockConfig.manageVatSubscriptionServicePath
        }

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe
            "We’ll send a confirmation letter to the agency address registered with HMRC within 15 working days."
        }

        "have the correct second paragraph" in {
          elementText(Selectors.paragraphTwo) shouldBe "We’ll also contact TheBusiness with an update."
        }
      }

      "the client's business name isn't retrieved" should {
        val viewModel = ChangeSuccessViewModel(exampleTitle, None, None, None)
        lazy val view = injectedView(viewModel)(request, messages, mockConfig, User("1111111111", arn = Some(BaseTestConstants.arn)))

        lazy implicit val document: Document = Jsoup.parse(view.body)

        "have the page title provided by the model" in {
          elementText(Selectors.title) shouldBe s"$exampleTitle - Your client’s VAT details - GOV.UK"
        }

        "have the heading provided by the model" in {
          elementText(Selectors.pageHeading) shouldBe exampleTitle
        }

        "have a finish button with the correct text" in {
          elementText(Selectors.button) shouldBe "Finish"
        }

        "have a finish button which navigates to the Change of Circs overview page" in {
          element(Selectors.button).select("a").attr("href") shouldBe mockConfig.manageVatSubscriptionServicePath
        }

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe "We’ll send a confirmation letter to the agency address registered with HMRC within 15 working days."
        }

        "have the correct second paragraph" in {
          elementText(Selectors.paragraphTwo) shouldBe "We’ll also contact your client with an update."
        }

      }
    }
  }
}