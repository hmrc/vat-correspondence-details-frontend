/*
 * Copyright 2023 HM Revenue & Customs
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

import mocks.{MockAuditingService, MockAuth, MockHttp}
import org.scalatest.matchers.should.Matchers
import play.api.http.Status
import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers._

trait ControllerBaseSpec extends MockAuth with MockHttp with MockAuditingService with Matchers {

  def unauthenticatedCheck(controllerAction: Action[AnyContent]): Unit = {

    "the user is not authenticated" should {

      "return 401 (Unauthorised)" in {
        mockMissingBearerToken
        val result = controllerAction(getRequest)
        status(result) shouldBe Status.UNAUTHORIZED
      }
    }
  }

  def insolvencyCheck(controllerAction: Action[AnyContent]): Unit = {

    "the user is insolvent and not continuing to trade" should {

      "return 403 (Forbidden)" in {
        val result = controllerAction(insolventRequest)
        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }
}

