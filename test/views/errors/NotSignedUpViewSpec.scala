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

package views.errors

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ViewBaseSpec

class NotSignedUpViewSpec extends ViewBaseSpec {

  "Rendering the unauthorised page" should {

    lazy val view = views.html.errors.not_signed_up()(request, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    s"have the correct document title" in {
      document.title shouldBe "You can not use this service yet"
    }

    s"have the correct page heading" in {
      elementText("#content h1") shouldBe "You can not use this service yet"
    }

    s"have the correct instructions on the page" in {
      elementText("#content p") shouldBe "You need to sign up to use software to submit your VAT Returns."
    }

  }
}
