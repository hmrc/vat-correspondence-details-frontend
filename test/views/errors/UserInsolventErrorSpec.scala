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

package views.errors

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import views.ViewBaseSpec
import views.html.errors.UserInsolventError

class UserInsolventErrorSpec extends ViewBaseSpec with Matchers {

  val userInsolvent: UserInsolventError = injector.instanceOf[UserInsolventError]

  "Rendering the unauthorised page" should {

    object Selectors {
      val pageHeading = "#insolvent-without-access-heading"
      val message = "#insolvent-without-access-body"
      val signOutLink = "#sign-out-link"
      val button = ".govuk-button"
    }

    lazy val view = userInsolvent()(user, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe "Sorry, you cannot access this service - Manage your VAT account - GOV.UK"
    }

    "have a the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe "Sorry, you cannot access this service"
    }

    "have the correct body" in {
      elementText(Selectors.message) shouldBe "Your business has been declared insolvent."
    }

    "have a sign out link" in {
      element(Selectors.signOutLink).attr("href") shouldBe controllers.routes.SignOutController.signOut(feedbackOnSignOut = false).url
    }

    "the sign out link should have the correct text" in {
      elementText(Selectors.signOutLink) shouldBe "Sign out"
    }

    "have the correct button text" in {
      elementText(Selectors.button) shouldBe "Go to your business tax account"
    }

    "have the correct button link" in {
      element(Selectors.button).attr("href") shouldBe "bta-home"
    }

  }


}
