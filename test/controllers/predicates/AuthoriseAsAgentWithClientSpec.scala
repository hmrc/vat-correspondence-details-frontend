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

import mocks.MockAuth
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.mvc.Results.Ok
import play.api.mvc.{Action, AnyContent}

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
        mockAgentAuthorised()
        val result = target(fakeRequestWithVrnAndRedirectUrl)
        status(result) shouldBe Status.OK
      }
    }

    "an agent has not selected their Client (No Client VRN in session)" should {

      lazy val result = target(request)

      "return 303 (SEE_OTHER) redirect" in {
        mockAgentAuthorised()
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the Select Your Client controller" in {
        //TODO re-add when redirect to VACLUF is added
        //redirectLocation(result) shouldBe Some(controllers.agent.routes.SelectClientVrnController.show().url)
      }
    }

    "the agent is not authenticated" should {

      "return 401 (Unauthorised)" in {
        mockMissingBearerToken()
        val result = target(fakeRequestWithVrnAndRedirectUrl)
        status(result) shouldBe Status.UNAUTHORIZED
      }
    }

    "the agent is not authorised" should {

      lazy val result = target(fakeRequestWithVrnAndRedirectUrl)

      "return 200" in {
        mockUnauthorised()
        status(result) shouldBe Status.OK
      }

      "page title is correct" in {
        Jsoup.parse(bodyOf(result)).title shouldBe "You are not authorised for this client"
      }

    }

    "the agent has no enrolments" should {

      lazy val result = await(target(fakeRequestWithClientsVRN))

      "return Internal Server Error (500)" in {
        mockAgentWithoutAffinity()
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "render the Internal Server Error page" in {
        Jsoup.parse(bodyOf(result)).title shouldBe "Sorry, we are experiencing technical difficulties - 500"
      }
    }
  }
}
