/*
 * Copyright 2022 HM Revenue & Customs
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

package pages.email

import common.SessionKeys
import forms.BouncedEmailForm
import models.customerInformation.{Add, Verify, VerifyAdd}
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse
import stubs.VatSubscriptionStub

class BouncedEmailPageSpec extends BasePageISpec {

  val path = "/fix-your-email"
  val email = "testemail@test.com"

  "Calling the Bounced Email (.show)" when {

    def show: WSResponse = get(path)

    "the user is authenticated" when {

      "user has an unverified email" should {

        "display the bounced email page" in {

          given.user.isAuthenticated
          VatSubscriptionStub.stubCustomerInfoEmailUnverified

          When("the Bounced email page is called")
          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(generateDocumentTitle("bouncedEmail.title"))
          )
        }
      }

      "user has a verified email" should {

        "display the VAT overview page" in {

          given.user.isAuthenticated
          VatSubscriptionStub.stubCustomerInfo

          When("user is verified and redirected to VAT overview")
          val result = show

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI("http://localhost:9152/vat-through-software/vat-overview")
          )
        }
      }
    }

    "the user is not enrolled to MTD VAT" should {

      "render the unauthorised page" in {

        given.user.isNotEnrolled

        When("the Bounced Email page is called but user is not enrolled")
        val result = show

        result should have(
          httpStatus(Status.FORBIDDEN),
          pageTitle(generateDocumentTitle("You can not use this service yet", isAgent = None))
        )
      }
    }
  }


  "Calling the Bounced email (.submit) route with an authenticated user" when {

    def submit(content: VerifyAdd): WSResponse = post(path, formatEmail(Some(email)) ++ Map(
      SessionKeys.validationEmailKey -> email))(toFormData(BouncedEmailForm.bouncedEmailForm, content))

    "user clicks on verify email" should {

      "redirect to the email verification page" in {

        given.user.isAuthenticated
        VatSubscriptionStub.stubCustomerInfoEmailUnverified

        When("user selects verify email")
        val res = submit(Verify)

        res should have(
          httpStatus(Status.SEE_OTHER),
          redirectURI("/vat-through-software/account/correspondence/send-passcode")
        )
      }
    }

    "user clicks on add email" should {

      "redirect to capture email page" in {

        given.user.isAuthenticated
        VatSubscriptionStub.stubCustomerInfoEmailUnverified

        When("user selects add a new email")
        val res = submit(Add)

        res should have(
          httpStatus(Status.SEE_OTHER),
          redirectURI("/vat-through-software/account/correspondence/change-email-address")
        )
      }
    }
  }
}
