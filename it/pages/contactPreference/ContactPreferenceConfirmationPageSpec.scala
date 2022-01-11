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

package pages.contactPreference

import models.contactPreferences.ContactPreference._
import pages.BasePageISpec
import play.api.http.Status
import stubs.VatSubscriptionStub

class ContactPreferenceConfirmationPageSpec extends BasePageISpec {

  val emailPath = "/confirmation-email-preference"
  val letterPath = "/confirmation-letter-preference"

  "Calling GET /confirmation-:changeType-preference" when {

    "the user is authenticated" when {

      "changeType is email" when {

        "session data is valid" should {

          "display the page correctly" in {

            given.user.isAuthenticated

            When("GET /confirmation-email-preference is called")
            val result = get(
              emailPath,
              formatCurrentContactPref(Some(paper)) ++
                formatValidationEmail(Some("asd@asd.com")) ++
                formatLetterToEmailPrefConfirmation(Some("true")) ++
                formatInflightChange(Some("false"))
            )

            result should have(
              httpStatus(Status.OK),
              pageTitle(generateDocumentTitle("contactPrefConfirmation.title"))
            )
          }
        }
      }

      "changeType is letter" when {

        "session data is valid" should {

          "display the page correctly" in {

            given.user.isAuthenticated

            When("GET /confirmation-letter-preference is called")

            And("Current address is retrieved")
            VatSubscriptionStub.stubCustomerInfo

            val result = get(
              letterPath,
              formatCurrentContactPref(Some(digital)) ++
                formatEmailToLetterPrefConfirmation(Some("true")) ++
                formatInflightChange(Some("false"))
            )

            result should have(
              httpStatus(Status.OK),
              pageTitle(generateDocumentTitle("contactPrefConfirmation.title"))
            )
          }
        }
      }
    }

    "a user is an authenticated agent" should {

      "redirect to Client's VAT Account" in {

        given.user.isAuthenticatedAgent

        When("GET /confirmation-email-preference is called")
        val result = get(emailPath)

        result should have(
          httpStatus(Status.SEE_OTHER),
          redirectURI("http://localhost:9149/vat-through-software/representative/client-vat-account")
        )
      }
    }

    "the user is not enrolled for MTD VAT" should {

      "render the unauthorised page" in {

        given.user.isNotEnrolled

        When("GET /confirmation-email-preference is called")
        val result = get(emailPath)

        result should have(
          httpStatus(Status.FORBIDDEN),
          pageTitle(generateDocumentTitle("You can not use this service yet", isAgent = None))
        )
      }
    }
  }
}
