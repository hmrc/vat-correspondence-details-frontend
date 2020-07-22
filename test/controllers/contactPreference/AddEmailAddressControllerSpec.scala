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
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.test.Helpers._
import views.html.contactPreference.AddEmailAddressView

class AddEmailAddressControllerSpec extends ControllerBaseSpec {

  lazy val controller = new AddEmailAddressController(mockErrorHandler, inject[AddEmailAddressView])

  "AddEmailAddressController .show is called" when {

    "user is authorised" when {

      "user has paper preference" when {

        "feature switch is on" should {

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
        }

        "feature switch is off" should {

          "return an NOT_FOUND result" in {
            lazy val result = {
              mockConfig.features.letterToConfirmedEmailEnabled(false)
              controller.show(requestWithPaperPref)
            }

            status(result) shouldBe NOT_FOUND
          }
        }
      }


      "user has a digital preference" should {
        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          controller.show(requestWithDigitalPref.withSession(SessionKeys.contactPrefUpdate -> "true"))
        }

        "return a 303 result" in {
          status(result) shouldBe SEE_OTHER
        }

        "bring the user back to BTA account details page" in {
          redirectLocation(result) shouldBe Some(mockConfig.btaAccountDetailsUrl)
        }
      }
    }

    "user is unauthorised" should {

      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(true)
        controller.show(request)
      }

      "return a 401 (Unauthorised) result" in {
        mockMissingBearerToken()
        status(result) shouldBe UNAUTHORIZED
      }
    }
  }
}

