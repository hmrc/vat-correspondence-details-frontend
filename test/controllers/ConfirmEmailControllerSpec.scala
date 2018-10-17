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

import play.api.http.Status
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import common.SessionKeys
import models.User
import org.jsoup.Jsoup

class ConfirmEmailControllerSpec extends ControllerBaseSpec {

  object TestConfirmEmailController extends ConfirmEmailController(
    mockAuthPredicate,
    messagesApi,
    mockConfig
  )

  val testVatNumber: String = "999999999"
  val testEmail: String = "test@email.co.uk"

  lazy val testGetRequest = FakeRequest("GET", "/confirm-email")

  "Calling the extractEmail function in ConfirmEmailController" when {

    "there is an authenticated request from a user with an email in session" should {
      "result in an email address being retrieved if there is an email" in {

        mockIndividualAuthorised()

        implicit val request: FakeRequest[AnyContentAsEmpty.type] = testGetRequest.withSession(
          SessionKeys.vatNumberKey -> testVatNumber, SessionKeys.emailKey -> testEmail)
        val user = User[AnyContent](testVatNumber, active = true, None)(request)

        TestConfirmEmailController.extractSessionEmail(user) shouldBe Some(testEmail)
      }
    }
  }

  "Calling the show action in ConfirmEmailController" when {

    "there is an email in session" should {
      "show the Confirm Email page" in {

        mockIndividualAuthorised()

        val request = testGetRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber, SessionKeys.emailKey -> testEmail)
        val result = TestConfirmEmailController.show(request)

        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "there isn't an email in session" should {
      "return OK" in {

        mockIndividualAuthorised()

        val request = testGetRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber, SessionKeys.emailKey -> "")
        val result = TestConfirmEmailController.show(request)

        status(result) shouldBe Status.OK
      }
    }

    "the user is not authorised" should {
      "show an internal server error" in {

        mockUnauthorised()

        val request = testGetRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber, SessionKeys.emailKey -> testEmail)
        val result = TestConfirmEmailController.show(request)

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        Jsoup.parse(bodyOf(result)).title shouldBe "Sorry, we are experiencing technical difficulties - 500"
      }
    }
  }

  "Calling the updateEmail action in ConfirmEmailController" when {

    "there is an email in session" should {
      "return OK" in {

        mockIndividualAuthorised()

        val request = testGetRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber, SessionKeys.emailKey -> testEmail)
        val result = TestConfirmEmailController.updateEmail(request)

        status(result) shouldBe Status.OK
      }
    }

    "there isn't an email in session" should {
      "return OK" in {

        mockIndividualAuthorised()

        val request = testGetRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber)
        val result = TestConfirmEmailController.updateEmail(request)

        status(result) shouldBe Status.OK
      }
    }

    "the user is not authorised" should {
      "show an internal server error" in {

        mockUnauthorised()

        val request = testGetRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber, SessionKeys.emailKey -> testEmail)
        val result = TestConfirmEmailController.updateEmail(request)

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        Jsoup.parse(bodyOf(result)).title shouldBe "Sorry, we are experiencing technical difficulties - 500"
      }
    }
  }
}
