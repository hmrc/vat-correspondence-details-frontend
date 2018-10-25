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

import org.jsoup.Jsoup
import play.api.http.Status
import play.api.test.Helpers._

class EmailChangeSuccessControllerSpec extends ControllerBaseSpec {

  object TestController extends EmailChangeSuccessController(
    mockAuthPredicate,
    messagesApi,
    mockConfig
  )

  "Calling the show action" when {

    "a user is enrolled with a valid enrolment" should {

      lazy val result = TestController.show(fakeRequestWithVrnAndRedirectUrl)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return 200" in {
        mockIndividualAuthorised()
        status(result) shouldBe Status.OK
      }
      "return HTML" in {
        mockIndividualAuthorised()
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "render the email change success page" in {
        mockIndividualAuthorised()
        document.select("h1").text() shouldBe "We have received the new email address"
      }
    }

    "a user is does not have a valid enrolment" should {

      lazy val result = TestController.show(fakeRequestWithVrnAndRedirectUrl)

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