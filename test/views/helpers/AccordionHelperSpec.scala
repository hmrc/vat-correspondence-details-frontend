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

package views.helpers

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.ViewBaseSpec
import views.html.helpers.AccordionHelper

class AccordionHelperSpec extends ViewBaseSpec {

  val testLabel: String = "test label"
  val testFeature: String = "test-feature"
  val testPageName: String = "test-page-name"
  val testHtml: Html = Html("test html")

  val injectedView: AccordionHelper = injector.instanceOf[AccordionHelper]
  lazy val view: Html = injectedView(testLabel, testFeature, testPageName, testHtml)

  lazy implicit val doc: Document = Jsoup.parse(view.body)

  "accordionHelper" should {
    "populate the relevant content in the correct positions" in {

      doc.getElementsByTag("summary").text() should include(testLabel)
      doc.getElementsByTag("details").text() should include(testHtml.toString())
    }
  }
}
