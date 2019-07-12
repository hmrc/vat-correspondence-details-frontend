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

package controllers

import org.jsoup.Jsoup
import play.api.http.Status
import play.api.test.Helpers._
import views.html.CaptureWebsiteView

class CaptureWebsiteControllerSpec extends ControllerBaseSpec {

  val view: CaptureWebsiteView = injector.instanceOf[CaptureWebsiteView]


  def setup(): Any = {

  }

  def target(): CaptureWebsiteController = {
    setup()

    new CaptureWebsiteController(
      mockAuthPredicateComponents,
      mockInflightPPOBPredicate,
      mcc,
      mockVatSubscriptionService,
      mockErrorHandler,
      mockAuditingService,
      view,
      mockConfig
    )
  }

  "Calling the show action" when {

    "a user is enrolled with a valid enrolment" when {

      lazy val result = target().show(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "there user has a website registered on ETMP" should {
        "return 200" in {
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "prepopulate the form with the validation email" in {
          document.select("input").attr("value") shouldBe "example.com"
        }
      }

    }

    "a user is does not have a valid enrolment" should {

      lazy val result = target().show(request)

      "return 403" in {
        mockIndividualWithoutEnrolment()
        status(result) shouldBe Status.FORBIDDEN
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }


    "a user is not logged in" should {

      lazy val result = target().show(request)

      "return 401" in {
        mockMissingBearerToken()
        status(result) shouldBe Status.UNAUTHORIZED
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }
}
