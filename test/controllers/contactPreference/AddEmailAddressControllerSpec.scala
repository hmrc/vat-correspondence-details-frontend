/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.contactPreference.AddEmailAddressView

class AddEmailAddressControllerSpec extends ControllerBaseSpec {

  lazy val controller = new AddEmailAddressController(mockErrorHandler, inject[AddEmailAddressView])
  lazy val getRequestWithSession: FakeRequest[AnyContentAsEmpty.type] = getRequest.withSession(SessionKeys.currentContactPrefKey -> paper)
  lazy val postRequestWithSession: FakeRequest[AnyContentAsEmpty.type] = postRequest.withSession(SessionKeys.currentContactPrefKey -> paper)

  "AddEmailAddressController .show is called" when {

    "user is authorised" when {

      "user has paper preference" should {

        lazy val result = {
          controller.show(getRequestWithSession.withSession(SessionKeys.contactPrefUpdate -> "true"))
        }

        "return an OK result" in {
          status(result) shouldBe OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }

      "user has a digital preference" should {
        lazy val result = {
          controller.show(getRequestWithDigitalPref.withSession(SessionKeys.contactPrefUpdate -> "true"))
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
        controller.show(getRequest)
      }

      "return a 401 (Unauthorised) result" in {
        mockMissingBearerToken
        status(result) shouldBe UNAUTHORIZED
      }
    }
  }

  "AddEmailAddressController .submit" when {

    "user is authorised" when {

      "the user currently has a digital preference" when {

        "'Yes' is submitted'" should {

          lazy val result = {
            controller.submit(postRequestWithSession.withFormUrlEncodedBody(yesNo -> yes))
          }

          "return a SEE_OTHER result" in {
            status(result) shouldBe SEE_OTHER
          }

          "redirect to confirmation page" in {
            redirectLocation(result) shouldBe Some(controllers.email.routes.CaptureEmailController.showPrefJourney.url)
          }
        }

        "'No' is submitted'" should {

          lazy val result = {
            controller.submit()(postRequestWithSession.withFormUrlEncodedBody(yesNo -> YesNoForm.no))
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
            controller.submit()(postRequestWithSession.withFormUrlEncodedBody())
          }

          "return a BAD_REQUEST result" in {
            status(result) shouldBe BAD_REQUEST
          }

          "render the view with errors" in {
            Jsoup.parse(contentAsString(result)).title should include("Error:")
          }
        }
      }

      "the user currently has a paper preference" should {

        lazy val result = {
          controller.submit(postRequest.withSession(SessionKeys.currentContactPrefKey -> digital))
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
        mockIndividualWithoutEnrolment
        controller.submit(postRequestWithSession)
      }

      "return a FORBIDDEN result" in {
        status(result) shouldBe FORBIDDEN
      }
    }
  }
}