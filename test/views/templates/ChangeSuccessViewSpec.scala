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

package views.templates

import models.contactPreferences.ContactPreference
import models.viewModels.ChangeSuccessViewModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import views.ViewBaseSpec
import views.html.templates.ChangeSuccessView

class ChangeSuccessViewSpec extends ViewBaseSpec with Matchers {

  val injectedView: ChangeSuccessView = inject[ChangeSuccessView]

  object Selectors {
    val title = "title"
    val pageHeading = "h1"
    val secondaryHeading = "h2"
    val paragraphOne = "#content > p:nth-child(3)"
    val paragraphTwo = "#content > p:nth-child(4)"
    val button = ".govuk-button"
  }

  val exampleTitle = "ExampleTitle"

  "The Change Successful view" when {

    "an individual is performing the action" when {

      "the contact preference is successfully retrieved" when {

        "the contact preference is Digital - email not verified" should {

          val viewModel = ChangeSuccessViewModel(exampleTitle, None, Some(ContactPreference.digital), None, None)
          lazy val view = injectedView(viewModel)(user, messages, mockConfig)

          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the page title provided by the model" in {
            elementText(Selectors.title) shouldBe s"$exampleTitle - Manage your VAT account - GOV.UK"
          }

          "have the heading provided by the model" in {
            elementText(Selectors.pageHeading) shouldBe exampleTitle
          }

          "have a finish button with the correct text" in {
            elementText(Selectors.button) shouldBe "Finish"
          }

          "have a finish button which navigates to the BTA account details page" in {
            element(Selectors.button).select("a").attr("href") shouldBe mockConfig.btaAccountDetailsUrl
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

        "the contact preference is Digital - verified email" should {
          val viewModel = ChangeSuccessViewModel(exampleTitle, None, Some(ContactPreference.digital), None, Some(true))
          lazy val view = injectedView(viewModel)(user, messages, mockConfig)
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the correct first paragraph" in {
            elementText(Selectors.paragraphOne) shouldBe "We’ll send you an email within 2 working days with" +
              " an update or you can check your HMRC secure messages."
          }
        }

        "the contact preference is Paper" should {
          val viewModel = ChangeSuccessViewModel(exampleTitle, None, Some(ContactPreference.paper), None, None)
          lazy val view = injectedView(viewModel)(user, messages, mockConfig)
          lazy implicit val document: Document = Jsoup.parse(view.body)

          "have the correct first paragraph" in {
            elementText(Selectors.paragraphOne) shouldBe "We’ll send a letter to your principal place of" +
              " business with an update within 15 working days."
          }
        }
      }

      "the contact preference is not retrieved" should {
        val viewModel = ChangeSuccessViewModel(exampleTitle, None, None, None, None)
        lazy implicit val document: Document = Jsoup.parse(injectedView(viewModel)(user, messages, mockConfig).body)

        "have the correct first paragraph" in {
          elementText(Selectors.paragraphOne) shouldBe "We will send you an update within 15 working days."
        }
      }
    }

    "an agent is performing the action" when {

        "agent has a digital preference" when {

          "client's business name is retrieved" should {

            val viewModel = ChangeSuccessViewModel(
              exampleTitle,
              agentEmail = Some("abc@digital.com"),
              None,
              businessName = Some("ABC Digital Ltd"),
              None
            )
            lazy val view = {
              injectedView(viewModel)(agent, messages, mockConfig)
            }
            lazy implicit val document: Document = Jsoup.parse(view.body)

            "have the correct first paragraph" in {
              elementText(Selectors.paragraphOne) shouldBe "We’ll send an email to abc@digital.com within 2 working days " +
                "telling you whether we can accept your request."
            }

            "have the correct second paragraph" in {
              elementText(Selectors.paragraphTwo) shouldBe "We’ll contact ABC Digital Ltd with an update."
            }
          }


          "client's business name is not retrieved" should {

            val viewModel = ChangeSuccessViewModel(
              exampleTitle,
              agentEmail = Some("abc@digital.com"),
              None,
              businessName = None,
              None
            )
            lazy val view = {
              injectedView(viewModel)(agent, messages, mockConfig)
            }
            lazy implicit val document: Document = Jsoup.parse(view.body)

            "have the correct first paragraph" in {
              elementText(Selectors.paragraphOne) shouldBe "We’ll send an email to abc@digital.com within 2 working days " +
                "telling you whether we can accept your request."
            }

            "have the correct second paragraph" in {
              elementText(Selectors.paragraphTwo) shouldBe "We’ll contact your client with an update."
            }
          }
        }

        "agent has no digital preference" when {

          "client's business name is retrieved" should {

            val viewModel = ChangeSuccessViewModel(
              exampleTitle,
              agentEmail = None,
              None,
              businessName = Some("ABC Digital Ltd"),
              None
            )
            lazy val view = {
              injectedView(viewModel)(agent, messages, mockConfig)
            }
            lazy implicit val document: Document = Jsoup.parse(view.body)

            "have the correct paragraph" in {
              elementText(Selectors.paragraphOne) shouldBe "We’ll contact ABC Digital Ltd with an update."
            }
          }

          "client's business name is not retrieved" should {

            val viewModel = ChangeSuccessViewModel(
              exampleTitle,
              agentEmail = None,
              None,
              businessName = None,
              None
            )
            lazy val view = {
              injectedView(viewModel)(agent, messages, mockConfig)
            }
            lazy implicit val document: Document = Jsoup.parse(view.body)

            "have the correct paragraph" in {
              elementText(Selectors.paragraphOne) shouldBe "We’ll contact your client with an update."
            }
          }
        }
    }
  }
}
