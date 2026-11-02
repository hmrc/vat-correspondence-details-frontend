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

package views

import config.ConfigKeys
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import play.twirl.api.Html
import uk.gov.hmrc.hmrcfrontend.views.Aliases.UserResearchBanner
import views.helpers.UrBannerUtil
import views.html.MainTemplate

class MainTemplateSpec extends ViewBaseSpec with Matchers {

  val injectedView: MainTemplate = inject[MainTemplate]
  val userBannerHeading: String = "You may receive a voucher for feedback"
  lazy val urBanner: UserResearchBanner = UrBannerUtil.getUrBanner()(mockConfig, messages)
  lazy val urBannerWithCloseButton: UserResearchBanner = UrBannerUtil.getUrBanner(hideCloseButton = false)(mockConfig, messages)

  override def afterEach(): Unit = {
    sys.props -= ConfigKeys.showUserResearchBannerEnabled
    super.afterEach()
  }

  object Selectors {
    val userResearchBanner = ".hmrc-user-research-banner"
    val userResearchBannerHeading = ".hmrc-user-research-banner__title"
    val userResearchBannerCloseButton = ".hmrc-user-research-banner__close"
  }

  "MainTemplate" when {

    "the user is an Agent" should {

      lazy val view = injectedView("Title")(Html(""))(agent, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct service name" in {
        elementText(".govuk-service-navigation__link") shouldBe "Your client’s VAT details"
      }

      "have the correct service URL" in {
        element(".govuk-service-navigation__link").attr("href") shouldBe mockConfig.vatAgentClientLookupAgentHubPath
      }
    }

    "the user is not an Agent" should {

      lazy val view = injectedView("Title")(Html(""))(user, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct service name" in {
        elementText(".govuk-service-navigation__link") shouldBe "Manage your VAT account"
      }

      "have the correct service URL" in {
        element(".govuk-service-navigation__link").attr("href") shouldBe mockConfig.vatOverviewUrl
      }
    }

    "the user type cannot be determined" should {

      lazy val view = injectedView("Title")(Html(""))(getRequest, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct service name" in {
        elementText(".govuk-service-navigation__text") shouldBe "VAT"
      }

      "have the correct service URL" in {
        element(".govuk-service-navigation__text").attr("href") shouldBe ""
      }
    }

    "the showUserResearchBanner feature switch is turned off" when {

      "a user research banner is provided" should {

        lazy val view = injectedView("Title", urBanner = Some(urBanner))(Html(""))(user, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "not display the user research banner" in {
          mockConfig.features.showUserResearchBannerEnabled(false)
          elementExtinct(Selectors.userResearchBanner)
        }
      }
    }

    "the showUserResearchBanner feature switch is turned on" when {

      "a user research banner without a close button is provided" should {

        lazy val view = injectedView("Title", urBanner = Some(urBanner))(Html(""))(user, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "display the correct user research banner heading" in {
          mockConfig.features.showUserResearchBannerEnabled(true)
          elementText(Selectors.userResearchBannerHeading) shouldBe userBannerHeading
        }

        "display the user research banner link with the configured url" in {
          mockConfig.features.showUserResearchBannerEnabled(true)
          element(s"${Selectors.userResearchBanner} a").attr("href") shouldBe mockConfig.urBannerUrl
        }

        "not display the close button" in {
          mockConfig.features.showUserResearchBannerEnabled(true)
          elementExtinct(Selectors.userResearchBannerCloseButton)
        }
      }

      "a user research banner with a close button is provided" should {

        lazy val view = injectedView("Title", urBanner = Some(urBannerWithCloseButton))(Html(""))(user, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "display the close button" in {
          mockConfig.features.showUserResearchBannerEnabled(true)
          elementExists(Selectors.userResearchBannerCloseButton)
        }
      }

      "no user research banner is provided" should {

        lazy val view = injectedView("Title")(Html(""))(user, messages, mockConfig)
        lazy implicit val document: Document = Jsoup.parse(view.body)

        "not display the user research banner" in {
          mockConfig.features.showUserResearchBannerEnabled(true)
          elementExtinct(Selectors.userResearchBanner)
        }
      }
    }
  }
}
