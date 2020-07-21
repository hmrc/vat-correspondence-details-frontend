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


import pages.BasePageISpec
import play.api.libs.ws.WSResponse
import play.api.http.Status
import models.contactPreferences.ContactPreference.paper


class AddEmailAddressPage extends BasePageISpec {

  val path = "/contact-preference/add-email-address"

  "Calling the AddEmailAddress .show method" when {

    def show: WSResponse = get(path, formatCurrentContactPref(Some(paper)))


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

      "render the Agent unauthorised page" in {

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
}
