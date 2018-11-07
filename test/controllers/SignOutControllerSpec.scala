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

import play.api.http.Status
import play.api.test.Helpers._


class SignOutControllerSpec extends ControllerBaseSpec {

  val controller = new SignOutController(messagesApi, mockConfig)

  "Navigating to the sign out page" when {

    "feedback on sign-out is enabled" should {

      "return 303" in {
        val result = controller.signOut(feedbackOnSignOut = true)(request)
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the correct location" in {
        val result = controller.signOut(feedbackOnSignOut = true)(request)
        println(mockConfig.feedbackSignOutUrl)
        redirectLocation(result) shouldBe Some(mockConfig.feedbackSignOutUrl)
      }
    }

    "feedback on sign-out is disabled" should {

      "return 303" in {
        val result = controller.signOut(feedbackOnSignOut = false)(request)
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the correct location" in {
        val result = controller.signOut(feedbackOnSignOut = false)(request)
        redirectLocation(result) shouldBe Some(mockConfig.unauthorisedSignOutUrl)
      }
    }
  }
}
