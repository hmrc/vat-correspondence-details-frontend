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

package controllers

import mocks.{MockAuditingService, MockAuth, MockHttp}
import play.api.http.Status
import play.api.mvc.{Action, AnyContent}

trait ControllerBaseSpec extends MockAuth with MockHttp with MockAuditingService {

  def unauthenticatedCheck(controllerAction: Action[AnyContent]): Unit = {

    "the user is not authenticated" should {

      "return 401 (Unauthorised)" in {
        mockMissingBearerToken()
        val result = controllerAction(request)
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

