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

package pages.contactNumbers

import common.SessionKeys.{validationLandlineKey, validationMobileKey}
import controllers.contactNumbers.routes
import forms.ContactNumbersForm.contactNumbersForm
import helpers.SessionCookieCrumbler
import models.customerInformation.ContactNumbers
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse
import stubs.VatSubscriptionStub._

class CaptureContactNumbersPageSpec extends BasePageISpec {

  val path = "/new-telephone-numbers"
  val newLandline = "01952654321"
  val newMobile = "07890654321"

  "Calling the Capture contact numbers (.show) route" when {

    def show: WSResponse = get(path)

    "the user is authenticated" when {

      "a success response with contact numbers is received from Vat Subscription" should {

        "load successfully" in {

          given.user.isAuthenticated
          stubCustomerInfo

          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(messages("captureContactNumbers.title"))
          )
        }

        "add the existing contact numbers to the session" in {

          given.user.isAuthenticated
          stubCustomerInfo

          val result = show

          SessionCookieCrumbler.getSessionMap(result).get(validationLandlineKey) shouldBe Some(currentLandline)
          SessionCookieCrumbler.getSessionMap(result).get(validationMobileKey) shouldBe Some(currentMobile)
        }
      }
    }
  }

  "Calling the Capture contact numbers (.submit) route" when {

    def submit(data: String): WSResponse = post(
      path, Map(validationLandlineKey -> currentLandline, validationMobileKey -> currentMobile)
    )(toFormData[ContactNumbers](
      contactNumbersForm(currentLandline, currentMobile), ContactNumbers(Some(newLandline), Some(newMobile)))
    )

    "the user is authenticated" when {

      "valid contact numbers are submitted" should {

        "redirect to the the Confirm Contact Numbers page" in {

          given.user.isAuthenticated
          stubCustomerInfo

          val result = submit(newLandline)

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(routes.ConfirmContactNumbersController.show().url)
          )
        }

        "add the existing contact number to session" in {

          given.user.isAuthenticated
          stubCustomerInfo

          val result = submit(newLandline)

          SessionCookieCrumbler.getSessionMap(result).get(validationMobileKey) shouldBe Some(currentMobile)
        }
      }
    }
  }
}
