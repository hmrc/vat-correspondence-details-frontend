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

import common.SessionKeys
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.test.Helpers._
import views.html.CaptureWebsiteView

class CaptureWebsiteControllerSpec extends ControllerBaseSpec {
  val testValidationWebsite: String = "https://www.current-valid-website.com"
  val testValidWebsite: String      = "https://www.new-valid-website.com"
  val testInvalidWebsite: String    = "invalid@£$%^&website"
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

        "prepopulate the form with the validation website" in {
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

  "Calling the submit action" when {

    "a user is enrolled with a valid enrolment" when {

      "there is a website in session" when {

        "the form is successfully submitted" should {

          lazy val result = target().submit(request
            .withFormUrlEncodedBody("website" -> testValidWebsite)
            .withSession(common.SessionKeys.validationWebsiteKey -> testValidationWebsite))

          "redirect to the confirm website view" in {
            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.ConfirmWebsiteController.show().url)
          }

          "add the new website to the session" in {
            session(result).get(SessionKeys.websiteKey) shouldBe Some(testValidWebsite)
          }
        }

//        "the form is unsuccessfully submitted" should {
//
//          lazy val result = target().submit(request
//            .withFormUrlEncodedBody("website" -> testInvalidWebsite)
//            .withSession(common.SessionKeys.validationWebsiteKey -> testValidationWebsite))
//
//          "reload the page with errors" in {
//            status(result) shouldBe Status.BAD_REQUEST
//          }
//
//          "return HTML" in {
//            contentType(result) shouldBe Some("text/html")
//            charset(result) shouldBe Some("utf-8")
//          }
//        }
      }

      "there is no website in session" when {

        lazy val result = target().submit(request)

        "render the error view" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }

    "a user is does not have a valid enrolment" should {

      lazy val result = target().submit(request)

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

      lazy val result = target().submit(request)

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
