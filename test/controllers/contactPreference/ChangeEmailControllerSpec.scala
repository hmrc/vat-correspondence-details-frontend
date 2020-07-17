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

package controllers.contactPreference

import common.SessionKeys
import controllers.ControllerBaseSpec
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import models.contactPreferences.ContactPreference._

class ChangeEmailControllerSpec extends ControllerBaseSpec {

  lazy val controller = new ChangeEmailController()
  lazy val requestWithSession: FakeRequest[AnyContentAsEmpty.type] = request.withSession((SessionKeys.currentContactPrefKey -> paper))

  "ChangeEmailController .show" when {

    "user is authorised" when {

      "the user currently has a digital preference" when {

        "the letterToConfirmedEmailEnabled feature switch is off" should {

          lazy val result = {
            mockConfig.features.letterToConfirmedEmailEnabled(false)
            controller.show(requestWithSession)
          }

          "return a NOT_FOUND result" in {
            status(result) shouldBe NOT_FOUND
          }
        }

        "the letterToConfirmedEmailEnabled feature switch is on" when {

          lazy val result = {
            mockConfig.features.letterToConfirmedEmailEnabled(true)
            controller.show(requestWithSession)
          }

          "return OK" in {
            status(result) shouldBe OK
          }
        }
      }

      "the user currently has a paper preference" should {

        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          controller.show(request.withSession(SessionKeys.currentContactPrefKey -> digital))
        }

        "return a SEE_OTHER result" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to BTA" in {
          redirectLocation(result) shouldBe Some(mockConfig.btaAccountDetailsUrl)
        }
      }
    }

    "user is unauthorised" should {
      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(true)
        mockIndividualWithoutEnrolment()
        controller.show(requestWithSession)
      }

      "return a FORBIDDEN result" in {
        status(result) shouldBe FORBIDDEN
      }
    }
  }
}
