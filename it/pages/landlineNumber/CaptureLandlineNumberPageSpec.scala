/*
 * Copyright 2023 HM Revenue & Customs
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

package pages.landlineNumber

import common.SessionKeys.validationLandlineKey
import controllers.landlineNumber.routes
import forms.LandlineNumberForm.landlineNumberForm
import helpers.SessionCookieCrumbler
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse
import stubs.VatSubscriptionStub._

class CaptureLandlineNumberPageSpec extends BasePageISpec {

  val path = "/new-landline-number"
  val newLandline = "01952654321"

  "Calling the Capture landline number (.show) route" when {

    def show: WSResponse = get(path, formatInflightChange(Some("false")) ++ Map(validationLandlineKey -> currentLandline))

    "the user is authenticated" when {

      "a success response with contact numbers is received from Vat Subscription" should {

        "load successfully" in {

          given.user.isAuthenticated
          stubCustomerInfo

          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(generateDocumentTitle("captureLandline.title"))
           )
        }
      }
    }
  }

  "Calling the Capture landline number (.submit) route" when {

    def submit(data: String): WSResponse = post(path, Map(
      validationLandlineKey -> currentLandline) ++ formatInflightChange(Some("false"))
    )(toFormData[String](landlineNumberForm(currentLandline), newLandline))

    "the user is authenticated" when {

      "a valid landline number has been submitted" should {

        "redirect to the the Confirm Landine Number page" in {

          given.user.isAuthenticated
          stubCustomerInfo

          val result = submit(newLandline)

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(routes.ConfirmLandlineNumberController.show.url)
          )
        }

        "add the existing landline number to session" in {

          given.user.isAuthenticated
          stubCustomerInfo

          val result = submit(newLandline)

          SessionCookieCrumbler.getSessionMap(result).get(validationLandlineKey) shouldBe Some(currentLandline)
        }
      }
    }
  }
}
