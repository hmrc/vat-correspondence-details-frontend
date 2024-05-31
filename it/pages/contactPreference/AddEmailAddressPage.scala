/*
 * Copyright 2024 HM Revenue & Customs
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

import forms.YesNoForm
import models.{Yes, YesNo, No}
import models.contactPreferences.ContactPreference.paper
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse

class AddEmailAddressPage extends BasePageISpec {

  val path = "/contact-preference/add-email-address"

  "Calling the AddEmailAddress .show method" when {

    def show: WSResponse = get(path, formatCurrentContactPref(Some(paper))++ formatInflightChange(Some("false")))

    "the user is authenticated" when {

      "the data is valid" should {

        "display the page correctly" in {

          given.user.isAuthenticated

          When("the AddEmailAddress page is called")
          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(generateDocumentTitle("cPrefAddEmail.title"))
          )
        }
      }
    }

    "a user is an authenticated agent" should {

      "redirect to client VAT account" in {

        given.user.isAuthenticatedAgent

        When("the AddEmailAddress page is called")
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

        When("the AddEmailAddress page is called")
        val result = show

        result should have(
          httpStatus(Status.FORBIDDEN),
          pageTitle(generateDocumentTitle("You can not use this service yet", isAgent = None))
        )
      }
    }
  }

  "Calling the AddEmailAddress .submit method" when {

    def submit(content: YesNo): WSResponse = post(
      path,
      formatCurrentContactPref(Some(paper)) ++ formatInflightChange(Some("false"))
    )(toFormData(YesNoForm.yesNoForm(""), content))

    "the user is authenticated" when {

      "the user selects Yes" should {

        "redirect to ChangeEmailController" in {

          given.user.isAuthenticated

          When("the AddEmailAddress page is submitted")
          val result = submit(Yes)

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI("/vat-through-software/account/correspondence/contact-preference/change-email-address")
          )
        }
      }

      "the user selects No" should {

        "redirect to BTA Manage Account" in {

          given.user.isAuthenticated

          When("the AddEmailAddress page is submitted")
          val result = submit(No)

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI("http://localhost:9020/business-account/manage-account/account-details")
          )
        }
      }
    }

    "a user is an agent" should {

      "redirect to BTA Manage Account" in {

        given.user.isAuthenticatedAgent

        When("the AddEmailAddress page is submitted")
        val result = submit(Yes)

        result should have(
          httpStatus(Status.SEE_OTHER),
          redirectURI("http://localhost:9149/vat-through-software/representative/client-vat-account")
        )
      }
    }

    "the user is not enrolled for MTD VAT" should {

      "render the unauthorised page" in {

        given.user.isNotEnrolled

        When("the AddEmailAddress page is submitted")
        val result = submit(Yes)

        result should have(
          httpStatus(Status.FORBIDDEN),
          pageTitle(generateDocumentTitle("You can not use this service yet", isAgent = None))
        )
      }
    }
  }
}
