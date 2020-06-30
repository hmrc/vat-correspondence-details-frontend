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

import controllers.ControllerBaseSpec
import play.api.http.Status
import play.api.http.Status.OK

class EmailPreferenceConfirmationControllerSpec extends ControllerBaseSpec {

  val controller = new EmailPreferenceConfirmationController(mockErrorHandler)

  ".show" should {

    "return an OK result when feature switch is true" in {
      val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(true)
        controller.show(fakeRequestWithClientsVRN)}

      status(result) shouldBe OK
    }

    "return a NOT_FOUND result when feature switch is false" in {
      val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(false)
        controller.show(fakeRequestWithClientsVRN)}

      status(result) shouldBe Status.NOT_FOUND
    }
  }
}
