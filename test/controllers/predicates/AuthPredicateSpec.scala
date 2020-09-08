/*
 * Copyright 2020 HM Revenue & Customs
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
import utils.MaterializerSupport
import play.api.test.Helpers._

import scala.concurrent.Future

class AuthPredicateSpec extends MockAuth with MaterializerSupport {

  val allowAgentPredicate: Action[AnyContent] = mockAuthPredicate.async {
    _ => Future.successful(Ok("test"))
  }

  "The AuthPredicateSpec" when {

    "the user is an Agent" when {

      "the Agent has an active HMRC-AS-AGENT enrolment" when {

        "the agent access feature is enabled" when {

          "the allowAgents parameter is set to true" should {

            "return OK (200)" in {
              mockConfig.features.agentAccessEnabled(true)
              mockAgentAuthorised()
              status(allowAgentPredicate(fakeRequestWithClientsVRN)) shouldBe Status.OK
            }
          }

          "the allowAgents parameter is set to false" should {

            val blockAgentPredicate: Action[AnyContent] =
              new AuthPredicate(mockAuthPredicateComponents, allowsAgents = false).async {
                _ => Future.successful(Ok("test"))
              }

            lazy val result = await(blockAgentPredicate(agent))

            "return Unauthorized (401)" in {
              mockAgentAuthorised()
              status(result) shouldBe Status.UNAUTHORIZED
            }

            "show the agent journey disabled page" in {
              messages(Jsoup.parse(bodyOf(result)).title) shouldBe "You cannot change your client’s email address yet - Your client’s VAT details - GOV.UK"
            }
          }

          "the allowAgents parameter is set to false and they are trying to access the changePref journey" should {

            val blockAgentPredicate: Action[AnyContent] =
              new AuthPredicate(mockAuthPredicateComponents, allowsAgents = false, true).async {
                _ => Future.successful(Ok("test"))
              }

            lazy val result = await(blockAgentPredicate(agent))

            "return SEE_OTHER (303)" in {
              mockAgentAuthorised()
              status(result) shouldBe Status.SEE_OTHER
            }

            "redirect to the agent hub page" in {
              redirectLocation(result) shouldBe Some("agent-hub")
            }
          }

          "the Agent does NOT have an Active HMRC-AS-AGENT enrolment" should {

            lazy val result = await(allowAgentPredicate(agent))

            "return Forbidden" in {
              mockConfig.features.agentAccessEnabled(true)
              mockAgentWithoutEnrolment()
              status(result) shouldBe Status.FORBIDDEN
            }

            "render the Unauthorised Agent page" in {
              messages(Jsoup.parse(bodyOf(result)).title) shouldBe "You can not use this service yet - Your client’s VAT details - GOV.UK"
            }
          }
        }

        "the agent access feature is disabled" should {

          lazy val result = await(allowAgentPredicate(agent))

          "return Unauthorized (401)" in {
            mockConfig.features.agentAccessEnabled(false)
            mockAgentAuthorised()
            status(result) shouldBe Status.UNAUTHORIZED
          }

          "show the agent journey disabled page" in {
            messages(Jsoup.parse(bodyOf(result)).title) shouldBe "You cannot change your client’s email address yet - Your client’s VAT details - GOV.UK"
          }
        }
      }

      "the agent does not have an affinity group" should {

        "return ISE (500)" in {
          mockUserWithoutAffinity()
          status(allowAgentPredicate(request)) shouldBe Status.INTERNAL_SERVER_ERROR
        }
      }

      "a no active session result is returned from Auth" should {

        lazy val result = await(allowAgentPredicate(agent))

        "return Unauthorised (401)" in {
          mockMissingBearerToken()
          status(result) shouldBe Status.UNAUTHORIZED
        }

        "render the Unauthorised page" in {
          messages(Jsoup.parse(bodyOf(result)).title) shouldBe "Your session has timed out - Your client’s VAT details - GOV.UK"
        }
      }

      "an authorisation exception is returned from Auth" should {

        lazy val result = await(allowAgentPredicate(agent))

        "return Internal Server Error (500)" in {
          mockAuthorisationException()
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "render the Unauthorised page" in {
          messages(Jsoup.parse(bodyOf(result)).title) shouldBe "There is a problem with the service - Your client’s VAT details - GOV.UK"
        }
      }
    }

    "the user is an Individual (Principle Entity)" when {

      "they have an active HMRC-MTD-VAT enrolment" should {

        "return OK (200)" in {
          mockIndividualAuthorised()
          status(allowAgentPredicate(request)) shouldBe Status.OK
        }
      }

      "they do NOT have an active HMRC-MTD-VAT enrolment" should {

        lazy val result = await(allowAgentPredicate(user))

        "return Forbidden (403)" in {
          mockIndividualWithoutEnrolment()
          status(result) shouldBe Status.FORBIDDEN
        }

        "render the Not Signed Up page" in {
          messages(Jsoup.parse(bodyOf(result)).title) shouldBe "You can not use this service yet - Business tax account - GOV.UK"
        }
      }
    }
  }
}
