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
import models.contactPreferences.ContactPreference.paper
import models.{No, Yes, YesNo}
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse

class EmailPreferencePageSpec extends BasePageISpec {

  val path = "/contact-preference-email"

  "Calling the EmailPreference .show method" when {

    def show: WSResponse = get(
      path,
      formatEmailPrefUpdate(Some("true")) ++
        formatCurrentContactPref(Some(paper)) ++
        formatInflightChange(Some("false"))
    )

    "the user is authenticated" when {

      "the data is valid" should {

        "display the page correctly" in {

          given.user.isAuthenticated

          When("the emailPreference page is called")
          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(generateDocumentTitle("emailPreference.title"))
          )
        }
      }

    }

    "a user is an authenticated agent" should {

      "render the Agent unauthorised page" in {

        given.user.isAuthenticatedAgent

        When("the EmailPreference page is called")
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

        When("the EmailPreference page is called")
        val result = show

        result should have(
          httpStatus(Status.FORBIDDEN),
          pageTitle(generateDocumentTitle("You can not use this service yet", isAgent = None))
        )
      }
    }
  }

  "Calling the EmailPreference .submit method with an authenticated user" when {

    def submit(data: YesNo): WSResponse = post(
      path,
      formatValidationEmail(Some("test@test.com")) ++
        formatCurrentContactPref(Some(paper)) ++
        formatInflightChange(Some("false"))
    )(toFormData(YesNoForm.yesNoForm("yesNoError"), data))

    "the user is authenticated" when {

      "the user selects 'Yes'" should {

        "redirect to the the Email to Use page" in {

          given.user.isAuthenticated

          When("'Yes' is selected")
          val res = submit(Yes)

          res should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(controllers.contactPreference.routes.EmailToUseController.show().url)
          )
        }

        s"add the ${SessionKeys.contactPrefUpdate} key and value to session" in {

          given.user.isAuthenticated

          When("'Yes' is selected")
          val result = submit(Yes)

          SessionCookieCrumbler.getSessionMap(result).get(SessionKeys.contactPrefUpdate) shouldBe Some("true")
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

          val res = post(
            path,
            formatValidationEmail(Some("test@test.com")) ++
              formatEmailPrefUpdate(Some("true")) ++
              formatCurrentContactPref(Some(paper)) ++
              formatInflightChange(Some("false"))
          )(Map("yes_no" -> Seq("")))

          res should have(
            httpStatus(Status.BAD_REQUEST),
            pageTitle("Error: " + generateDocumentTitle("emailPreference.title")),
            elementText(".error-message")("Select yes if you want communications by email")
          )
        }
      }
    }
  }
}
