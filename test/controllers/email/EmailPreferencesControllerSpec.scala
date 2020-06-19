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

package controllers.email

import controllers.ControllerBaseSpec
import play.api.http.Status.{OK, NOT_FOUND}

class EmailPreferencesControllerSpec extends ControllerBaseSpec {

  val controller = new EmailPreferenceController(mockErrorHandler)

  "The letterToConfirmedEmailEnabled feature switch is off" when {

    ".show is called" should {

      "return an OK result" in {
        val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(false)
          controller.show(fakeRequestWithClientsVRN)
        }

        status(result) shouldBe NOT_FOUND
      }
    }

    ".submit is called" should {

      "return an OK result" in {

        val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(false)
          controller.submit(fakeRequestWithClientsVRN)
        }

        status(result) shouldBe NOT_FOUND
      }
    }

  }

  "The letterToConfirmedEmailEnabled feature switch is on" when {

    ".show is called" should {

      "return an OK result" in {
        val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          controller.show(fakeRequestWithClientsVRN)
        }

        status(result) shouldBe OK
      }
    }

    ".submit is called" should {

      "return an OK result" in {

        val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          controller.submit(fakeRequestWithClientsVRN)
        }

        status(result) shouldBe OK
      }
    }

  }

}
