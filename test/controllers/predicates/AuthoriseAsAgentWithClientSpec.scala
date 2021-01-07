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

package controllers.predicates

import mocks.MockAuth
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.mvc.Results.Ok
import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers._

import scala.concurrent.Future

class AuthoriseAsAgentWithClientSpec extends MockAuth {

  def target: Action[AnyContent] =
    mockAuthAsAgentWithClient.async {
      implicit user =>
        Future.successful(Ok(s"test ${user.vrn}"))
    }

  "The AuthoriseAsAgentWithClientSpec" when {

    "the agent is authorised with a Client VRN in session" should {

      "return 200" in {
        mockConfig.features.agentAccessEnabled(true)

        mockAgentAuthorised()
        val result = target(fakeRequestWithClientsVRN)
        status(result) shouldBe Status.OK
      }
    }

    "the agent is not authenticated" should {

      "return 401 (Unauthorised)" in {
        mockConfig.features.agentAccessEnabled(true)

        mockMissingBearerToken()
        val result = target(fakeRequestWithClientsVRN)
        status(result) shouldBe Status.UNAUTHORIZED
      }
    }

    "the agent is not authorised" should {

      mockConfig.features.agentAccessEnabled(true)
      lazy val result = target(fakeRequestWithClientsVRN)

      "return 303" in {
        mockAuthorisationException()
        status(result) shouldBe Status.SEE_OTHER
      }

      "have the correct redirect location" in {
        redirectLocation(result) shouldBe Some(mockConfig.vatAgentClientLookupUnauthorised)
      }
    }

    "the agent has no enrolments" should {

      mockConfig.features.agentAccessEnabled(true)
      lazy val result = await(target(fakeRequestWithClientsVRN))

      "return Internal Server Error (500)" in {
        mockAgentWithoutAffinity()
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "render the Internal Server Error page" in {
        messages(Jsoup.parse(bodyOf(result)).title) shouldBe "There is a problem with the service - VAT - GOV.UK"
      }
    }

    "there is no client VRN in session" should {

      mockConfig.features.agentAccessEnabled(true)
      mockAgentAuthorised()
      lazy val result = await(target(request))

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the agent client lookup service" in {
        redirectLocation(result) shouldBe Some(mockConfig.vatAgentClientLookupServicePath)
      }
    }
  }
}
