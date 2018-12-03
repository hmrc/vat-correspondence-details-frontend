/*
 * Copyright 2018 HM Revenue & Customs
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

package pages

import common.SessionKeys
import forms.EmailForm
import helpers.SessionCookieCrumbler
import play.api.http.Status
import play.api.libs.ws.WSResponse
import stubs.VatSubscriptionStub

class CaptureEmailPageSpec extends BasePageISpec {

  val path = "/change-email-address"

  "Calling the Capture email (.show)" when {

    def show: WSResponse = get(path, formatInflightPPOB(Some("false")))

    "the user is authenticated" when {

      "a success response is received for Customer Details which" should {

        "contain an email address" in {

          given.user.isAuthenticated

          And("a successful response for an individual is stubbed")
          VatSubscriptionStub.stubCustomerInfo

          When("the Capture email page is called")
          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(messages("captureEmail.title"))
          )
        }

        "not contain a valid response" in {

          given.user.isAuthenticated

          And("a success response for Customer Details is stubbed")
          VatSubscriptionStub.stubCustomerInfoNoEmailJson

          When("the Capture email page is called")
          val result = show

          result should have(
            httpStatus(Status.INTERNAL_SERVER_ERROR),
            pageTitle("Sorry, we are experiencing technical difficulties - 500")
          )
        }
      }

      "an error response is received for Customer Details" in {

        given.user.isAuthenticated

        And("an error response for an individual is stubbed")
        VatSubscriptionStub.stubCustomerInfoError

        When("the Capture email page is called")
        val result = show

        result should have(
          httpStatus(Status.INTERNAL_SERVER_ERROR),
          pageTitle("Sorry, we are experiencing technical difficulties - 500")
        )
      }
    }

    "a user is an authenticated agent" should {

      "render the Agent unauthorised page" in {

        given.user.isAuthenticatedAgent

        When("the Capture email page is called")
        val result = show

        result should have(
          httpStatus(Status.UNAUTHORIZED),
          pageTitle("You cannot change your client’s correspondence details yet")
        )
      }
    }

    "the user is not enrolled for MTD VAT" should {

      "render the unauthorised page" in {

        given.user.isNotEnrolled

        When("the Capture email page is called")
        val result = show

        result should have(
          httpStatus(Status.FORBIDDEN),
          pageTitle("You can not use this service yet")
        )
      }
    }
  }

  "Calling the Capture email (.submit) route with an authenticated user" when {

    val currentEmail = "test@test.com"
    val newEmail = "pepsi-mac@test.com"

    def submit(data: String): WSResponse = post(
      path, formatValidationEmail(Some(currentEmail)) ++ formatInflightPPOB(Some("false"))
    )(toFormData(EmailForm.emailForm(currentEmail), data))

    "the user is authenticated" when {

      "a valid email address is submitted" should {

        "redirect to the the Confirm Email page" in {

          given.user.isAuthenticated

          And("a successful response for an individual is stubbed")
          VatSubscriptionStub.stubCustomerInfo

          When("a valid email is submitted")
          val res = submit(newEmail)

          res should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(controllers.routes.ConfirmEmailController.show().url)
          )
        }

        "add the email to session" in {

          given.user.isAuthenticated

          And("a successful response for an individual is stubbed")
          VatSubscriptionStub.stubCustomerInfo

          When("a valid email is submitted")
          val res = submit(newEmail)

          SessionCookieCrumbler.getSessionMap(res).get(SessionKeys.emailKey) shouldBe Some(newEmail)
        }
      }
    }
  }
}
