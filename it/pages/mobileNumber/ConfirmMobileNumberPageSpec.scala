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

import common.SessionKeys
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse
import stubs.VatSubscriptionStub

class ConfirmMobileNumberPageSpec extends BasePageISpec {

  val path = "/confirm-new-mobile-number"
  val path_update = "/update-new-mobile-number"
  val newMobile = "012345678910"

  "Calling the Confirm Mobile Number (.show) route" when {

    def show(sessionKeys: (String, String)*): WSResponse =
      get(path, Map(sessionKeys: _*) ++ formatInflightChange(Some("false")))

    "the user is a authenticated" when {

      "there is a new mobile in the session" should {

        "load successfully" in {

          given.user.isAuthenticated

          val result = show(SessionKeys.prepopulationMobileKey -> newMobile)

          result should have(
            httpStatus(Status.OK),
            pageTitle(generateDocumentTitle("checkYourAnswers.title"))
          )
        }
      }
    }
  }
  "Calling the Update Mobile Number route" when {

    def show(sessionKeys: (String, String)*): WSResponse =
      get(path_update, Map(sessionKeys: _*) ++ formatInflightChange(Some("false")))

    "the mobile is updated" should {

      "redirect to the confirmation screen" in {
        given.user.isAuthenticated
        VatSubscriptionStub.stubCustomerInfo
        VatSubscriptionStub.stubUpdatePPOB

        val result = show(SessionKeys.prepopulationMobileKey -> newMobile)

        result should have(
          httpStatus(Status.SEE_OTHER),
          redirectURI(controllers.routes.ChangeSuccessController.mobileNumber().url)
        )
      }
    }
  }
}
