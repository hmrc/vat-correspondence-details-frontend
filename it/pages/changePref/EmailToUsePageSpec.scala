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

package pages.changePref

import assets.BaseITConstants.internalServerErrorTitle
import common.SessionKeys
import forms.YesNoForm
import helpers.SessionCookieCrumbler
import models.{No, Yes, YesNo}
import pages.BasePageISpec
import play.api.libs.ws.WSResponse
import play.api.http.Status
import stubs.VatSubscriptionStub

class EmailToUsePageSpec extends BasePageISpec {

  val path = "/preference-confirm-email"

  "Calling the EmailToUse .show method" when {

    def show: WSResponse = get(path)

    "the user is authenticated" when {

      "a success response is received for Customer Details" when {

        "the data is valid" should {

          "display the page correctly" in {

            given.user.isAuthenticated

            And("a successful response for an individual is stubbed")
            VatSubscriptionStub.stubCustomerInfo

            When("the emailToUse page is called")
            val result = show

            result should have(
              httpStatus(Status.OK),
              pageTitle(generateDocumentTitle("emailToUse.title"))
            )
          }

          "add the email to session" in {

            given.user.isAuthenticated

            And("a successful response for an individual is stubbed")
            VatSubscriptionStub.stubCustomerInfo

            When("a valid email is submitted")
            val res = show

            SessionCookieCrumbler.getSessionMap(res).get(SessionKeys.prepopulationEmailKey) shouldBe Some("testemail@test.com")
            SessionCookieCrumbler.getSessionMap(res).get(SessionKeys.validationEmailKey) shouldBe Some("testemail@test.com")
          }
        }

        "the data is invalid" should {

          "return an error if invalid data is received" in {

            given.user.isAuthenticated

            And("a success response for Customer Details with invalid data is stubbed")
            VatSubscriptionStub.stubCustomerInfoInvalidJson

            When("the emailToUse page is called")
            val result = show

            result should have(
              httpStatus(Status.INTERNAL_SERVER_ERROR),
              pageTitle(generateDocumentTitle(internalServerErrorTitle))
            )
          }
        }


      }

      "an error response is received for Customer Details" in {

        given.user.isAuthenticated

        And("an error response for an individual is stubbed")
        VatSubscriptionStub.stubCustomerInfoError

        When("the emailToUse page is called")
        val result = show

        result should have(
          httpStatus(Status.INTERNAL_SERVER_ERROR),
          pageTitle(generateDocumentTitle(internalServerErrorTitle))
        )
      }
    }

    "a user is an authenticated agent" should {

      "render the Agent unauthorised page" in {

        given.user.isAuthenticatedAgent

        When("the EmailToUse page is called")
        val result = show

        result should have(
          httpStatus(Status.UNAUTHORIZED),
          elementText("h1")("You cannot change your client’s email address yet")
        )
      }
    }

    "the user is not enrolled for MTD VAT" should {

      "render the unauthorised page" in {

        given.user.isNotEnrolled

        When("the EmailToUse page is called")
        val result = show

        result should have(
          httpStatus(Status.FORBIDDEN),
          pageTitle(generateDocumentTitle("You can not use this service yet", isAgent = None))
        )
      }
    }
  }

  "Calling the EmailToUse .submit method with an authenticated user" when {

    def submit(data: YesNo): WSResponse =
      post(path, formatValidationEmail(Some("test@test.com")))(toFormData(YesNoForm.yesNoForm("yesNoError"), data))

    "the user is authenticated" when {

      "the user selects 'Yes'" should {

        "redirect to the the Confirm Email page" in {

          given.user.isAuthenticated

          When("'Yes' is selected")
          val res = submit(Yes)

          res should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(controllers.email.routes.CaptureEmailController.show().url)
          )
        }
      }

      "the user selects 'No'" should {

        "redirect to the the Confirm Email page" in {

          given.user.isAuthenticated

          When("'No' is selected")
          val res = submit(No)

          res should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(controllers.email.routes.CaptureEmailController.show().url)
          )
        }
      }

      "the user has not selected an answer" should {

        "return 400 BAD_REQUEST" in {

          given.user.isAuthenticated

          post(path, formatValidationEmail(Some("test@test.com")))(Map("yes_no" -> Seq(""))) should have(
            httpStatus(Status.BAD_REQUEST),
            pageTitle("Error: " + generateDocumentTitle("emailToUse.title")),
            elementText(".error-message")("Select yes if this is the email address you want us to use")
          )
        }
      }
    }
  }

}
