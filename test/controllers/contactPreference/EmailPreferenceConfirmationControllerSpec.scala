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
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK, SEE_OTHER}
import views.html.contactPreference.PreferenceConfirmationView
import common.SessionKeys.validationEmailKey
import play.api.test.Helpers._

class EmailPreferenceConfirmationControllerSpec extends ControllerBaseSpec {

  val controller = new EmailPreferenceConfirmationController(mockErrorHandler, inject[PreferenceConfirmationView])

  ".show" when {

    "the feature switch is false" should {

      "return a NOT_FOUND result" in {

        val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(false)
          controller.show(fakeRequestWithClientsVRN)}

        status(result) shouldBe NOT_FOUND

      }

    }

    "the feature switch is true" should {

      "return an OK result if an email is in session" in {

        val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          controller.show(fakeRequestWithClientsVRN.withSession(validationEmailKey -> "asd@asd.com", SessionKeys.contactPrefConfirmed -> "true"))}

        status(result) shouldBe OK

      }

      "return an internal server error if there is no email in session" in {

        val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          controller.show(request.withSession(SessionKeys.contactPrefConfirmed -> "true"))
        }

        status(result) shouldBe INTERNAL_SERVER_ERROR

      }

      s"return a Redirect if the ${SessionKeys.contactPrefConfirmed} key is not in session" in {
        val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          controller.show(request)
        }

        status(result) shouldBe SEE_OTHER

        redirectLocation(result) shouldBe Some(controllers.contactPreference.routes.EmailToUseController.show().url)
      }

    }

  }
}
