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

package controllers

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.play.test.UnitSpec

import common.SessionKeys
import forms.EmailForm._

import scala.concurrent.Future
import org.jsoup.Jsoup

class CaptureEmailControllerSpec extends ControllerBaseSpec {

  val testValidEmail: String   = "test@example.com"
  val testInvalidEmail: String = "invalidEmail"

  object TestCaptureEmailController extends CaptureEmailController(
    mockAuthPredicate,
    messagesApi,
    mockConfig
  )

  "Calling the show action" when {

    "a user is enrolled with a valid enrolment" should {

      lazy val result = TestCaptureEmailController.show(fakeRequestWithVrnAndRedirectUrl)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "a user is does not have a valid enrolment" should {

      lazy val result = TestCaptureEmailController.show(fakeRequestWithVrnAndRedirectUrl)

      "return 403" in {
        mockIndividualWithoutEnrolment()
        status(result) shouldBe Status.FORBIDDEN
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }

  "Calling the submit action" when {

    "a user is enrolled with a valid enrolment" when {

      "the form is successfully submitted" should {

        lazy val result = TestCaptureEmailController.submit(request
          .withFormUrlEncodedBody("email" -> testValidEmail))
        lazy val document = Jsoup.parse(bodyOf(result))

        "redirect to the hello_world view" in {
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HelloWorldController.helloWorld().url)
        }

        "contain the email in session" in {
          await(result).session(request).get(SessionKeys.emailKey) shouldBe Some(testValidEmail)
        }
      }

      "the form is unsuccessfully submitted" should {

        lazy val result = TestCaptureEmailController.submit(request
          .withFormUrlEncodedBody("email" -> testInvalidEmail))
        lazy val document = Jsoup.parse(bodyOf(result))

        "reload the page with errors" in {
          status(result) shouldBe Status.BAD_REQUEST
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }

    "a user is does not have a valid enrolment" should {

      lazy val result = TestCaptureEmailController.submit(request
        .withFormUrlEncodedBody("email" -> testValidEmail))

      "return 403" in {
        mockIndividualWithoutEnrolment()
        status(result) shouldBe Status.FORBIDDEN
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }
}
