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

package controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.{AffinityGroup, MissingBearerToken}
import uk.gov.hmrc.auth.core.retrieve.Retrieval

import scala.concurrent.Future


class SignOutControllerSpec extends ControllerBaseSpec {

  val controller = new SignOutController(mcc, mockEnrolmentsAuthService)

  def mockAuth(authResult: Future[Option[AffinityGroup]]): Any =
    when(mockAuthConnector.authorise(any(), any[Retrieval[Option[AffinityGroup]]]())(any(), any()))
      .thenReturn(authResult)

  "The .signOut action" when {

    "feedback on sign-out is enabled" when {

      "the user is an agent" should {

        lazy val result: Future[Result] = {
          mockAuth(Future.successful(Some(AffinityGroup.Agent)))
          controller.signOut(feedbackOnSignOut = true)(request)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to the correct survey url" in {
          redirectLocation(result) shouldBe Some(mockConfig.feedbackSignOutUrl("VATCA"))
        }
      }

      "the user is a principal entity" should {

        lazy val result: Future[Result] = {
          mockAuth(Future.successful(Some(AffinityGroup.Individual)))
          controller.signOut(feedbackOnSignOut = true)(request)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to the correct survey url" in {
          redirectLocation(result) shouldBe Some(mockConfig.feedbackSignOutUrl("VATC"))
        }
      }

      "there is an authorisation exception" should {

        lazy val result: Future[Result] = {
          mockAuth(Future.failed(MissingBearerToken()))
          controller.signOut(feedbackOnSignOut = true)(request)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to the unauthorised sign out URL" in {
          redirectLocation(result) shouldBe Some(mockConfig.unauthorisedSignOutUrl)
        }
      }
    }

    "feedback on sign-out is disabled" should {

      lazy val result: Future[Result] = controller.signOut(feedbackOnSignOut = false)(request)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the unauthorised sign out URL" in {
        redirectLocation(result) shouldBe Some(mockConfig.unauthorisedSignOutUrl)
      }
    }
  }
}
