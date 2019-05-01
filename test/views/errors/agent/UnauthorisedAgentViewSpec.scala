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

package views.errors.agent

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.errors.agent.UnauthorisedAgentView

class UnauthorisedAgentViewSpec extends ViewBaseSpec {

  val injectedView: UnauthorisedAgentView = injector.instanceOf[UnauthorisedAgentView]

  object Selectors {
    val heading = "h1"
    val instructions = "#content article p"
    val signOutButton = ".button"
    val signOutLink = "#content article p a"
  }

  "Rendering the agent unauthorised page" should {

    lazy implicit val document: Document = Jsoup.parse(injectedView().body)

    "have the correct document title" in {
      document.title shouldBe "You can not use this service yet"
    }

    "have the correct page heading" in {
      elementText(Selectors.heading) shouldBe "You can not use this service yet"
    }

    "have the correct instructions on the page" in {
      elementText(Selectors.instructions) shouldBe "To use this service, you need to set up an agent services account."
    }

    "have a link to sign out" in {
      element(Selectors.signOutLink).attr("href") shouldBe mockConfig.agentServicesGovUkGuidance
    }

    "have a sign out button" in {
      element(Selectors.signOutButton).text() shouldBe "Sign out"
    }

    "have a sign out button which allows the user to sign out" in {
      element(Selectors.signOutButton).attr("href") shouldBe
        controllers.routes.SignOutController.signOut(feedbackOnSignOut = false).url
    }
  }
}
