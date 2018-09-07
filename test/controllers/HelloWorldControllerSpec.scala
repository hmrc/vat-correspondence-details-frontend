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

class HelloWorldControllerSpec extends ControllerBaseSpec {

  object TestHelloWorldController extends HelloWorldController(
    mockAuthPredicate,
    messagesApi,
    mockConfig
  )

  "Calling the helloWorld action" when {

    "a user is enrolled with a valid enrolment" should {

      lazy val result = TestHelloWorldController.helloWorld(fakeRequestWithVrnAndRedirectUrl)
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

      "render the Hello world page" in {
        mockIndividualAuthorised()
        document.select("h1").text() shouldBe "Hello from vat-correspondence-details-frontend!"
      }
    }

    "a user is does not have a valid enrolment" should {

      lazy val result = TestHelloWorldController.helloWorld(fakeRequestWithVrnAndRedirectUrl)

      "return 403" in {
        mockAgentWithoutEnrolment()
        status(result) shouldBe Status.FORBIDDEN
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }
}