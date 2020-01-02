/*
 * Copyright 2020 HM Revenue & Customs
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

package views.errors

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.errors.SessionTimeoutView

class SessionTimeoutViewSpec extends ViewBaseSpec {

  val injectedView: SessionTimeoutView = injector.instanceOf[SessionTimeoutView]

  object Selectors {
    val heading = "#content h1"
    val instructions = "#content article p"
    val signInLink = "#content article a:nth-of-type(1)"
  }

  "Rendering the session timeout page" should {

    lazy implicit val document: Document = Jsoup.parse(injectedView().body)

    "have the correct document title" in {
      document.title shouldBe "Your session has timed out - VAT - GOV.UK"
    }

    "have the correct page heading" in {
      elementText(Selectors.heading) shouldBe "Your session has timed out"
    }

    "have the correct instructions on the page" in {
      elementText(Selectors.instructions) shouldBe "To manage your VAT account, you will have to sign in using your Government Gateway ID."
    }

    "have a link to the correct sign in url" in {
      element(Selectors.signInLink).attr("href") shouldBe mockConfig.signInContinueUrl

    }
  }
}
