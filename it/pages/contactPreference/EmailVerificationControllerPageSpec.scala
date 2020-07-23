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

package pages.contactPreference

import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse
import stubs.VatSubscriptionStub

class EmailVerificationControllerPageSpec extends BasePageISpec {

  val verifyEmailPath = "/contact-preference/verify-email-address"
  val email = "test@test.com"

  "Calling the Verify Email Address route" when {

    "the user is authenticated" when {

      "there is an email in session" should {

        def show: WSResponse = get(verifyEmailPath, formatEmail(Some(email)) ++ formatInflightChange(Some("false")))

        "render the verify email view" in {

          given.user.isAuthenticated

          And("a successful response for an individual is stubbed")
          VatSubscriptionStub.stubCustomerInfo

          When("the Verify email page is called")
          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(generateDocumentTitle("verifyEmail.title"))
          )
        }
      }
    }

    "there is not an email in session" should {

      def show: WSResponse = get(verifyEmailPath, formatInflightChange(Some("false")))

      "redirect to the Capture Email Address controller" in {

        given.user.isAuthenticated

        When("the Verify email page is called")
        val result = show

        result should have(
          httpStatus(Status.SEE_OTHER),
          redirectURI("/")
        )
      }
    }

    "the user is an authenticated agent" should {

      def show: WSResponse = get(verifyEmailPath, formatEmail(Some(email)))

      "render the Agent unauthorised view" in {

        given.user.isAuthenticatedAgent

        When("the Verify email page is called")
        val result = show

        result should have(
          httpStatus(Status.UNAUTHORIZED),
          elementText("h1")("You cannot change your client’s email address yet")
        )
      }
    }

    "the user is not enrolled for MTD VAT" should {

      def show: WSResponse = get(verifyEmailPath, formatEmail(Some(email)))

      "render the unauthorised view" in {

        given.user.isNotEnrolled

        When("the Verify email page is called")
        val result = show

        result should have(
          httpStatus(Status.FORBIDDEN),
          pageTitle(generateDocumentTitle("You can not use this service yet", isAgent = None))
        )
      }
    }
  }

}
