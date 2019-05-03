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

package controllers.predicates

import mocks.MockAuth
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.mvc.Results.Ok
import play.api.mvc.{Action, AnyContent}
import utils.MaterializerSupport

import scala.concurrent.Future

class AuthPredicateSpec extends MockAuth with MaterializerSupport {

  def target: Action[AnyContent] = mockAuthPredicate.async {
    implicit request => Future.successful(Ok("test"))
  }

  "The AuthPredicateSpec" when {

    "agent access is enabled" when {

      "the user does not have affinity group" should {

        "return ISE (500)" in {
          mockConfig.features.agentAccessEnabled(true)
          mockUserWithoutAffinity()
          status(target(request)) shouldBe Status.INTERNAL_SERVER_ERROR
        }
      }

      "the user is an Agent" when {


        "the Agent has an Active HMRC-AS-AGENT enrolment" when {

          "a successful authorisation result is returned from Auth" should {

            "return OK (200)" in {
              mockConfig.features.agentAccessEnabled(true)
              mockAgentAuthorised()
              status(target(fakeRequestWithClientsVRN)) shouldBe Status.OK
            }
          }

          "a no active session result is returned from Auth" should {

            mockConfig.features.agentAccessEnabled(true)
            lazy val result = await(target(fakeRequestWithClientsVRN))

            "return Unauthorised (401)" in {
              mockMissingBearerToken()
              status(result) shouldBe Status.UNAUTHORIZED
            }

            "render the Unauthorised page" in {
              messages(Jsoup.parse(bodyOf(result)).title) shouldBe "Your session has timed out"
            }
          }

          "an authorisation exception is returned from Auth" should {

            mockConfig.features.agentAccessEnabled(true)
            lazy val result = await(target(fakeRequestWithClientsVRN))

            "return Internal Server Error (500)" in {
              mockAuthorisationException()
              status(result) shouldBe Status.INTERNAL_SERVER_ERROR
            }

            "render the Unauthorised page" in {
              messages(Jsoup.parse(bodyOf(result)).title) shouldBe "Sorry, we are experiencing technical difficulties - 500"
            }
          }
        }

        "the Agent does NOT have an Active HMRC-AS-AGENT enrolment" should {

          mockConfig.features.agentAccessEnabled(true)
          lazy val result = await(target(fakeRequestWithClientsVRN))

          "return Forbidden" in {
            mockAgentWithoutEnrolment()
            status(result) shouldBe Status.FORBIDDEN
          }

          "render the Unauthorised Agent page" in {
            messages(Jsoup.parse(bodyOf(result)).title) shouldBe "You can not use this service yet"
          }
        }
      }

    }


    "the user is an Individual (Principle Entity)" when {

      "they have an active HMRC-MTD-VAT enrolment" should {

        "return OK (200)" in {
          mockIndividualAuthorised()
          status(target(request)) shouldBe Status.OK
        }
      }

      "they do NOT have an active HMRC-MTD-VAT enrolment" should {

        lazy val result = await(target(request))

        "return Forbidden (403)" in {
          mockIndividualWithoutEnrolment()
          status(result) shouldBe Status.FORBIDDEN
        }

        "render the Not Signed Up page" in {
          messages(Jsoup.parse(bodyOf(result)).title) shouldBe "You can not use this service yet"
        }
      }
    }

    "agent access is disabled" should {
      "show agent journey disabled page" in {
        mockConfig.features.agentAccessEnabled(false)
        mockAgentAuthorised()
        val result = target(request)
        status(result) shouldBe Status.UNAUTHORIZED
        messages(Jsoup.parse(bodyOf(result)).title) shouldBe "You cannot change your client’s correspondence details yet"
      }
    }
  }
}
