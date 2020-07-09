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
import forms.YesNoForm.{yes, yesNo, no => _no}
import models.contactPreferences.ContactPreference.paper
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK, SEE_OTHER}
import play.api.test.Helpers._
import views.html.contactPreference.EmailPreferenceView

class EmailPreferenceControllerSpec extends ControllerBaseSpec {

  lazy val controller = new EmailPreferenceController(mockErrorHandler, inject[EmailPreferenceView])

  "The letterToConfirmedEmailEnabled feature switch is off" when {

    ".show is called" should {

      "return an NOT_FOUND result" in {
        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(false)
          controller.show(requestWithPaperPref)
        }

        status(result) shouldBe NOT_FOUND
      }
    }

    ".submit is called" should {

      "return an NOT_FOUND result" in {

        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(false)
          controller.submit(requestWithPaperPref)
        }

        status(result) shouldBe NOT_FOUND
      }
    }

  }

  "The letterToConfirmedEmailEnabled feature switch is on" when {

    ".show is called" should {

      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(true)
        controller.show(requestWithPaperPref.withSession(SessionKeys.contactPrefUpdate -> "true"))
      }

      "return an OK result" in {
        status(result) shouldBe OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      s"not contain the session key ${SessionKeys.contactPrefUpdate}" in {
        session(result).get(SessionKeys.contactPrefUpdate) shouldBe None
      }

      "add the current contact preference to session" in {
        session(result).get(SessionKeys.currentContactPrefKey) shouldBe Some(paper)
      }
    }

    ".submit is called with a Yes" should {

      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(true)
        controller.submit(requestWithPaperPref.withFormUrlEncodedBody(yesNo -> yes))
      }

      "return a SEE_OTHER result" in {
        status(result) shouldBe SEE_OTHER
      }

      "be at the correct url" in {
        redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/preference-confirm-email")
      }

      s"a value is added to the ${SessionKeys.contactPrefUpdate} key" in {
        session(result).get(SessionKeys.contactPrefUpdate) shouldBe Some("true")
      }

    }

    ".submit is called with a No" should {

      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(true)
        controller.submit(requestWithPaperPref.withFormUrlEncodedBody(yesNo -> _no))
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
          controller.submit(requestWithPaperPref.withFormUrlEncodedBody())
        }

        status(result) shouldBe BAD_REQUEST
      }

    }

  }

}
