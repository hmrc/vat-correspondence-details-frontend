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

package controllers.website

import assets.BaseTestConstants.vrn
import audit.models.ChangeWebsiteAddressStartAuditModel
import common.SessionKeys
import controllers.ControllerBaseSpec
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.test.Helpers._
import views.html.website.CaptureWebsiteView

class CaptureWebsiteControllerSpec extends ControllerBaseSpec {
  val testValidationWebsite: String = "https://www.current~valid-website.com"
  val testValidWebsite: String      = "https://www.new~valid-website.com"
  val testInvalidWebsite: String    = "invalid@£$%^&website"

  def target(): CaptureWebsiteController = {
    new CaptureWebsiteController(
      mockVatSubscriptionService,
      mockErrorHandler,
      mockAuditingService,
      injector.instanceOf[CaptureWebsiteView]
    )
  }

  "Calling the show action" when {

    "a user is enrolled with a valid enrolment" when {

      "the user's current website is retrieved from session" should {

        lazy val result =
          target().show(getRequest.withSession(common.SessionKeys.validationWebsiteKey -> testValidationWebsite))

        lazy val document = Jsoup.parse(contentAsString(result))

        "return 200" in {
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "prepopulate the form with the user's current website" in {
          document.select("#website").attr("value") shouldBe testValidationWebsite
        }

        "audit the correct information for the journey start event" in {
          verifyExtendedAudit(ChangeWebsiteAddressStartAuditModel(Some(testValidationWebsite), vrn, None))
        }
      }
    }

    "the previous form value is retrieved from session" should {

      lazy val result = target().show(getRequest.withSession(
        common.SessionKeys.validationWebsiteKey -> testValidationWebsite,
        common.SessionKeys.prepopulationWebsiteKey -> testValidWebsite)
      )
      lazy val document = Jsoup.parse(contentAsString(result))

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "prepopulate the form with the previously entered form value" in {
        document.select("#website").attr("value") shouldBe testValidWebsite
      }

      "audit the correct information for the journey start event" in {
        verifyExtendedAudit(ChangeWebsiteAddressStartAuditModel(Some(testValidationWebsite), vrn, None))
      }
    }

    "the user has no current website address (inflight predicate has set a blank string in session)" should {

      lazy val result =
        target().show(getRequest.withSession(common.SessionKeys.validationWebsiteKey -> ""))
      lazy val document = Jsoup.parse(contentAsString(result))

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "not prepopulate the form" in {
        document.select("#website").attr("value") shouldBe ""
      }

      "audit the correct information for the journey start event" in {
        verifyExtendedAudit(ChangeWebsiteAddressStartAuditModel(None, vrn, None))
      }
    }

    "there is no website in session" when {

      lazy val result = target().show(getRequest)

      "return 500" in {
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "a user does not have a valid enrolment" should {

      lazy val result = target().show(getRequest)

      "return 403" in {
        mockIndividualWithoutEnrolment
        status(result) shouldBe Status.FORBIDDEN
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "a user is not logged in" should {

      lazy val result = target().show(getRequest)

      "return 401" in {
        mockMissingBearerToken
        status(result) shouldBe Status.UNAUTHORIZED
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    insolvencyCheck(target().show())
  }

  "Calling the submit action" when {

    "a user is enrolled with a valid enrolment" when {

      "there is a website in session" when {

        "the form is successfully submitted" should {

          lazy val result = target().submit(postRequest
            .withFormUrlEncodedBody("website" -> testValidWebsite)
            .withSession(common.SessionKeys.validationWebsiteKey -> testValidationWebsite))

          "redirect to the confirm website view" in {
            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.ConfirmWebsiteController.show.url)
          }

          "add the new website to the session" in {
            session(result).get(SessionKeys.prepopulationWebsiteKey) shouldBe Some(testValidWebsite)
          }
        }

        "the form is unsuccessfully submitted" should {

          lazy val result = target().submit(postRequest
            .withFormUrlEncodedBody("website" -> testInvalidWebsite)
            .withSession(common.SessionKeys.validationWebsiteKey -> testValidationWebsite))

          "reload the page with errors" in {
            status(result) shouldBe Status.BAD_REQUEST
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
            charset(result) shouldBe Some("utf-8")
          }
        }
      }

      "there is no website in session" when {

        lazy val result = target().submit(postRequest)


        "render the error view" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }

    "a user does not have a valid enrolment" should {

      lazy val result = target().submit(postRequest)

      "return 403" in {
        mockIndividualWithoutEnrolment
        status(result) shouldBe Status.FORBIDDEN
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "a user is not logged in" should {

      lazy val result = target().submit(postRequest)

      "return 401" in {
        mockMissingBearerToken
        status(result) shouldBe Status.UNAUTHORIZED
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    insolvencyCheck(target().submit)
  }
}
