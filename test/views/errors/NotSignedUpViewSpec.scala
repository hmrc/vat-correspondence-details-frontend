/*
 * Copyright 2021 HM Revenue & Customs
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
import views.html.errors.NotSignedUpView

class NotSignedUpViewSpec extends ViewBaseSpec {

  val injectedView: NotSignedUpView = injector.instanceOf[NotSignedUpView]

  object Selectors {
    val heading = "#content h1"
    val instructions = "#content article p"
    val signUpLink = "#content > article > p> a"
    val signUpButton = "#content .button"
  }

  "Rendering the unauthorised page" should {

    lazy implicit val document: Document = Jsoup.parse(injectedView().body)

    "have the correct document title" in {
      document.title shouldBe "You can not use this service yet - VAT - GOV.UK"
    }

    "have the correct page heading" in {
      elementText(Selectors.heading) shouldBe "You can not use this service yet"
    }

    "have the correct instructions on the page" in {
      elementText(Selectors.instructions) shouldBe "You need to sign up to use software to submit your VAT Returns."
    }

    "have a link to sign up" in {
      document.select(Selectors.signUpLink).first().attr("href") shouldBe
        "https://www.gov.uk/guidance/use-software-to-submit-your-vat-returns"
    }

    "have a sign out button which allows the user to sign out" in {
      element(Selectors.signUpButton).attr("href") shouldBe
        controllers.routes.SignOutController.signOut(feedbackOnSignOut = false).url
    }
  }
}
