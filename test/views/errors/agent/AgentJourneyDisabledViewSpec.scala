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

package views.errors.agent

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec

class AgentJourneyDisabledViewSpec extends ViewBaseSpec {

  object Selectors {
    val heading = "h1"
    val summary = "p:nth-child(2)"
    val instructions = "p:nth-child(3)"
    val signOutButton = ".button"
  }

  "Rendering the agent journey disabled page" should {

    lazy val view = views.html.errors.agent.agentJourneyDisabled()
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe "You cannot change your client’s correspondence details yet"
    }

    "have a the correct page heading" in {
      elementText(Selectors.heading) shouldBe "You cannot change your client’s correspondence details yet"
    }

    "have the correct summary message on the page" in {
      elementText(Selectors.summary) shouldBe "Agents cannot change their client’s correspondence details yet."
    }

    "have the correct instructions on the page" in {
      elementText(Selectors.instructions) shouldBe "If your client needs to change their correspondence details, " +
        "they need to sign in using their own Government Gateway details."
    }

    "have a sign out button" in {
      elementText(Selectors.signOutButton) shouldBe "Sign out"
    }

    s"have a link to sign out" in {
      element(Selectors.signOutButton).attr("href") shouldBe controllers.routes.SignOutController.signOut().url
    }
  }

}