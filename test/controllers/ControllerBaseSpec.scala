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

import mocks.{MockAuth, MockHttp}
import play.api.http.Status
import play.api.mvc.{Action, AnyContent}
import utils.MaterializerSupport

trait ControllerBaseSpec extends MockAuth with MockHttp with MaterializerSupport {

  def unauthenticatedCheck(controllerAction: Action[AnyContent]): Unit = {

    "the user is not authenticated" should {

      "return 401 (Unauthorised)" in {
        mockMissingBearerToken()
        val result = controllerAction(request)
        status(result) shouldBe Status.UNAUTHORIZED
      }
    }
  }
}

