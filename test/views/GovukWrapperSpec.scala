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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.GovukWrapper

class GovukWrapperSpec extends ViewBaseSpec {

  lazy val injectedView: GovukWrapper = injector.instanceOf[GovukWrapper]
  val accessibilityLinkSelector = "#footer > div > div > div.footer-meta-inner > ul > li:nth-child(2) > a"

  "The GOV UK Wrapper" when {

    "the user" should {

      lazy val view = injectedView(mockConfig,"")
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "not be shown a logo" in {
        document.select(".organisation-logo") shouldBe empty
      }

      "have the correct Accessibility link" in {
        element(accessibilityLinkSelector).attr("href") shouldBe "/vat-through-software/accessibility-statement"
      }

      "have the correct text for Accessibility link" in {
        elementText(accessibilityLinkSelector) shouldBe "Accessibility"
      }
    }
  }
}
