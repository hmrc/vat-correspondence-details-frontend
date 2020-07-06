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
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK, SEE_OTHER}
import play.api.test.Helpers._
import forms.YesNoForm.{yes, yesNo, no => _no}

class LetterPreferenceControllerSpec extends ControllerBaseSpec {

  lazy val controller = new LetterPreferenceController(mockErrorHandler)

  "the letterToConfirmedEmailEnabled feature switch is off" when {


    ".show() is called" should {

      "return a NOT_FOUND result" in {

        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(false)
          controller.show(request)
        }

        status(result) shouldBe NOT_FOUND
      }
    }

    ".submit() is called" should {

      "return a NOT_FOUND result" in {
        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(false)
          controller.submit(request)
        }

        status(result) shouldBe NOT_FOUND
      }
    }

  }

  "the letterToConfirmedEmailEnabled feature switch is on" when {

    ".show() is called" should {

      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(true)
        controller.show(request.withSession(SessionKeys.contactPrefUpdate -> "true"))
      }
      "return an OK result" in {

        status(result) shouldBe OK

      }

      s"not contain the session key ${SessionKeys.contactPrefUpdate}" in {
        session(result).get(SessionKeys.contactPrefUpdate) shouldBe None
      }


    }

    ".submit() is called with a yes" should {

      lazy val result ={

        mockConfig.features.letterToConfirmedEmailEnabled(true)
        controller.submit(request.withFormUrlEncodedBody(yesNo -> yes))
      }


      "return a SEE_OTHER result" in {

        status(result) shouldBe SEE_OTHER

      }

    }

    ".submit() is called with a no" should {

      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(true)
        controller.submit(request.withFormUrlEncodedBody(yesNo -> _no))
      }

      "return a SEE_OTHER result" in {

        status(result) shouldBe SEE_OTHER
      }

      "be at the correct URL" in {
        redirectLocation(result) shouldBe Some(mockConfig.dynamicJourneyEntryUrl(false))
      }

    }

    ".submit() is called with no form data" should {

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
