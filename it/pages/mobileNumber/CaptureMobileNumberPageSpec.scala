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

package pages.mobileNumber

import common.SessionKeys.validationMobileKey
import controllers.mobileNumber.routes
import forms.MobileNumberForm.mobileNumberForm
import helpers.SessionCookieCrumbler
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse
import stubs.VatSubscriptionStub._

class CaptureMobileNumberPageSpec extends BasePageISpec {

  val path = "/new-mobile-number"
  val newMobile = "07573492831"

  "Calling the Capture mobile number (.show) route" when {

    def show: WSResponse = get(path, formatInflightChange(Some("false")) ++ Map(validationMobileKey -> currentMobile))

    "the user is authenticated" when {

      "a success response with contact numbers is received from Vat Subscription" should {

        "load successfully" in {

          given.user.isAuthenticated
          stubCustomerInfo

          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(generateDocumentTitle("captureMobile.title"))
           )
        }
      }
    }
  }

  "Calling the Capture mobile number (.submit) route" when {

    def submit(data: String): WSResponse = post(path, Map(
      validationMobileKey -> currentMobile) ++ formatInflightChange(Some("false"))
    )(toFormData[String](mobileNumberForm(currentMobile), newMobile))

    "the user is authenticated" when {

      "a valid mobile number has been submitted" should {

        "redirect to the the Confirm Mobile Number page" in {

          given.user.isAuthenticated
          stubCustomerInfo

          val result = submit(newMobile)

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(routes.ConfirmMobileNumberController.show.url)
          )
        }

        "add the existing mobile number to session" in {

          given.user.isAuthenticated
          stubCustomerInfo

          val result = submit(newMobile)

          SessionCookieCrumbler.getSessionMap(result).get(validationMobileKey) shouldBe Some(currentMobile)
        }
      }
    }
  }
}
