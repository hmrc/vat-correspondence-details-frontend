/*
 * Copyright 2019 HM Revenue & Customs
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

import common.SessionKeys
import forms.YesNoForm
import helpers.SessionCookieCrumbler
import models.contactPreferences.ContactPreference.digital
import models.{No, Yes, YesNo}
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse
import stubs.VatSubscriptionStub

class LetterPreferencePageSpec extends BasePageISpec {

  val path = "/contact-preference-letter"

  "Calling the LetterPreference .show method" when {

    def show: WSResponse = get(path, formatCurrentContactPref(Some(digital)))

    "the user is authenticated" should {

      "display the page correctly" in {

        given.user.isAuthenticated

        And("a successful response for an individual is stubbed")
        VatSubscriptionStub.stubCustomerInfo

        val result = show

        result should have(
          httpStatus(Status.OK),
          pageTitle(generateDocumentTitle("letterPreference.title"))
        )
      }
    }

    "a user is an agent" should {

      "redirect to Agent overview" in {

        given.user.isAuthenticatedAgent

        val result = show

        result should have(
          httpStatus(Status.SEE_OTHER),
          redirectURI("http://localhost:9149/vat-through-software/representative/client-vat-account")
        )
      }
    }

    "the user is not enrolled for MTD VAT" should {

      "render the unauthorised page" in {

        given.user.isNotEnrolled

        val result = show

        result should have(
          httpStatus(Status.FORBIDDEN),
          pageTitle(generateDocumentTitle("You can not use this service yet", isAgent = None))
        )
      }
    }
  }

  "Calling the LetterPreference .submit method with an authenticated user" when {

    def submit(data: YesNo): WSResponse = post(
      path,
      formatCurrentContactPref(Some(digital))
    )(toFormData(YesNoForm.yesNoForm("yesNoError"), data))

    "the user is authenticated" when {

      "the user selects 'Yes'" should {

        "redirect to Contact Preference Confirmation page" in {

          given.user.isAuthenticated

          When("'Yes' is selected")
          val res = submit(Yes)

          res should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(controllers.contactPreference.routes.ContactPreferenceConfirmationController.show("letter").url)
          )

          SessionCookieCrumbler.getSessionMap(res).get(SessionKeys.contactPrefUpdate) shouldBe Some("true")
        }
      }

      "the user selects 'No'" should {

        "redirect to the the account details page" in {

          given.user.isAuthenticated

          When("'No' is selected")
          val res = submit(No)

          res should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI("http://localhost:9020/business-account/manage-account/account-details")
          )
        }
      }

      "the user has not selected an answer" should {

        "return 400 BAD_REQUEST" in {

          given.user.isAuthenticated

          And("a successful response for an individual is stubbed")
          VatSubscriptionStub.stubCustomerInfo

          val res = post(
            path,
            formatCurrentContactPref(Some(digital)))(Map("yes_no" -> Seq(""))
          )

          res should have(
            httpStatus(Status.BAD_REQUEST),
            pageTitle("Error: " + generateDocumentTitle("letterPreference.title")),
            elementText(".error-message")("Select yes if you want communications by letter")
          )
        }
      }
    }
  }
}