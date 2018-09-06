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

package controllers.predicates

import controllers.ControllerBaseSpec
import play.api.http.Status
import play.api.mvc.Results.Ok
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.Future

class AuthoriseAsAgentOnlySpec extends ControllerBaseSpec {

  def target: Action[AnyContent] = {
    mockAgentOnlyAuthPredicate.async {
      implicit request => Future.successful(Ok("test"))
    }
  }

  "The AuthoriseAsAgentOnlySpec" when {

    "the user is an Agent" when {

      "the Agent is signed up to HMRC-AS-AGENT" should {

        "return 200" in {
          mockAgentAuthorised()
          val result = target(request)
          status(result) shouldBe Status.OK
        }

        "return a body" in {
          mockAgentAuthorised()
          val result = await(bodyOf(target(request)))
          result shouldBe "test"
        }
      }

      "the Agent is not signed up to HMRC_AS_AGENT" should {

        lazy val result = target(request)

        "return Forbidden (403)" in {
          mockAgentWithoutEnrolment()
          status(result) shouldBe Status.FORBIDDEN
        }

        "render the Unauthorised Agent page" in {
          //TODO: Readd when AgentUnauthorisedPage has been added
          //Jsoup.parse(bodyOf(result)).title shouldBe AgentUnauthorisedPageMessages.title
        }
      }
    }

    "the user is not an Agent" should {

      "return 403 (Forbidden)" in {
        mockIndividualAuthorised()
        val result = target(request)
        status(result) shouldBe Status.FORBIDDEN
      }
    }

    "the user does not have an affinity group" should {

      "return 500 (Internal server error)" in {
        mockUserWithoutAffinity()
        val result = target(request)
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }

    "a user with no active session" should {

      "return 401 (Unauthorized)" in {
        mockMissingBearerToken()
        val result = target(request)
        status(result) shouldBe Status.UNAUTHORIZED
      }
    }

    "a user with an authorisation exception" should {

      "return 403 (Forbidden)" in {
        mockUnauthorised()
        val result = target(request)
        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }
}
