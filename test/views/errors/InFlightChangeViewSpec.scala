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

package views.errors

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec
import views.html.errors.InFlightChangeView

class InFlightChangeViewSpec extends ViewBaseSpec {

  val injectedView: InFlightChangeView = injector.instanceOf[InFlightChangeView]

  object Selectors {
    val heading = "h1"
    val paragraphOne = "article > p:nth-child(3)"
    val paragraphTwo = "article > p:nth-child(4)"
    val backLink = ".link-back"
  }

  "The PPOB change pending view" should {

    lazy implicit val document: Document = Jsoup.parse(injectedView().body)

    "have the correct title" in {
      document.title shouldBe "We are reviewing your request"
    }

    "have the correct heading" in {
      elementText(Selectors.heading) shouldBe "You already have a change pending"
    }

    "have the correct information in the first paragraph" in {
      elementText(Selectors.paragraphOne) shouldBe "You have already requested to change the principal place " +
        "of business. While this change is pending you are unable to change your email address."
    }

    "have the correct information in the second paragraph" in {
      elementText(Selectors.paragraphTwo) shouldBe "We will usually update your details within 2 working days."
    }

    "have the correct text for the back link" in {
      elementText(Selectors.backLink) shouldBe "Back"
    }

    "have the correct back link location" in {
      element(Selectors.backLink).attr("href") shouldBe "mockManageVatOverviewUrl"
    }
  }
}
