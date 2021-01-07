/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.YesNoForm
import forms.YesNoForm.{yes, yesNo}
import models.contactPreferences.ContactPreference._
import org.jsoup.Jsoup
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK, SEE_OTHER}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{redirectLocation, _}
import views.html.contactPreference.AddEmailAddressView

class AddEmailAddressControllerSpec extends ControllerBaseSpec {

  lazy val controller = new AddEmailAddressController(mockErrorHandler, inject[AddEmailAddressView])
  lazy val requestWithSession: FakeRequest[AnyContentAsEmpty.type] = request.withSession(SessionKeys.currentContactPrefKey -> paper)

  "AddEmailAddressController .show is called" when {

    "user is authorised" when {

      "user has paper preference" when {

        "feature switch is on" should {

          lazy val result = {
            mockConfig.features.letterToConfirmedEmailEnabled(true)
            controller.show(requestWithSession.withSession(SessionKeys.contactPrefUpdate -> "true"))
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
              controller.show(requestWithSession)
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

  "AddEmailAddressController .submit" when {

    "user is authorised" when {

      "the user currently has a digital preference" when {

        "the letterToConfirmedEmailEnabled feature switch is off" should {

          lazy val result = {
            mockConfig.features.letterToConfirmedEmailEnabled(false)
            controller.submit(requestWithSession)
          }

          "return a NOT_FOUND result" in {
            status(result) shouldBe NOT_FOUND
          }
        }

        "the letterToConfirmedEmailEnabled feature switch is on" when {

          "'Yes' is submitted'" should {

            lazy val result = {
              mockConfig.features.letterToConfirmedEmailEnabled(true)
              controller.submit(requestWithSession.withFormUrlEncodedBody(yesNo -> yes))
            }

            "return a SEE_OTHER result" in {
              status(result) shouldBe SEE_OTHER
            }

            "redirect to confirmation page" in {
              redirectLocation(result) shouldBe Some(controllers.email.routes.CaptureEmailController.showPrefJourney().url)
            }
          }

          "'No' is submitted'" should {

            lazy val result = {
              mockConfig.features.letterToConfirmedEmailEnabled(true)
              controller.submit()(requestWithSession.withFormUrlEncodedBody(yesNo -> YesNoForm.no))
            }

            "return a SEE_OTHER result" in {
              status(result) shouldBe SEE_OTHER
            }

            "redirect to BTA" in {
              redirectLocation(result) shouldBe Some(mockConfig.btaAccountDetailsUrl)
            }
          }

          "Nothing is submitted (form has errors)" when {

            lazy val result = {
              mockConfig.features.letterToConfirmedEmailEnabled(true)
              controller.submit()(requestWithSession.withFormUrlEncodedBody())
            }

            "return a BAD_REQUEST result" in {
              status(result) shouldBe BAD_REQUEST
            }

            "render the view with errors" in {
              Jsoup.parse(bodyOf(result)).title should include("Error:")
            }
          }
        }
      }

      "the user currently has a paper preference" should {

        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          controller.submit(request.withSession(SessionKeys.currentContactPrefKey -> digital))
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
        controller.submit(requestWithSession)
      }

      "return a FORBIDDEN result" in {
        status(result) shouldBe FORBIDDEN
      }
    }
  }
}