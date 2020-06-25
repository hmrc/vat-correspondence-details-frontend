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
import forms.YesNoForm.{yesNo, yes, no =>_no}
import play.api.http.Status.{NOT_FOUND, OK, SEE_OTHER, BAD_REQUEST}
import play.api.test.Helpers._

class EmailPreferencesControllerSpec extends ControllerBaseSpec {

  lazy val controller = new EmailPreferenceController(mockErrorHandler)

  "The letterToConfirmedEmailEnabled feature switch is off" when {

    ".show is called" should {

      "return an OK result" in {
        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(false)
          controller.show(request)
        }

        status(result) shouldBe NOT_FOUND
      }
    }

    ".submit is called" should {

      "return an OK result" in {

        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(false)
          controller.submit(request)
        }

        status(result) shouldBe NOT_FOUND
      }
    }

  }

  "The letterToConfirmedEmailEnabled feature switch is on" when {

    ".show is called" should {

      "return an OK result" in {
        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          controller.show(request)
        }

        status(result) shouldBe OK
      }
    }

    ".submit is called with a Yes" should {

      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(true)
        controller.submit(request.withFormUrlEncodedBody(yesNo -> yes))
      }

      "return a SEE_OTHER result" in {
        status(result) shouldBe SEE_OTHER
      }

      "be at the correct url" in {
        redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/preference-confirm-email")
      }

    }

    ".submit is called with a No" should {

      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(true)
        controller.submit(request.withFormUrlEncodedBody(yesNo -> _no))
      }

      "return a SEE_OTHER result" in {
        status(result) shouldBe SEE_OTHER
      }

      "be at the correct url" in {
        redirectLocation(result) shouldBe Some(mockConfig.dynamicJourneyEntryUrl(false))
      }

    }

    ".submit is called with no form data" should {

      "return a BAD_REQUEST result" in {

        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          controller.submit(request.withFormUrlEncodedBody())
        }

        status(result) shouldBe BAD_REQUEST
      }

    }

  }

}
