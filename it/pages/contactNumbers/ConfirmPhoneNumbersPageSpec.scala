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

import common.SessionKeys
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse
import stubs.VatSubscriptionStub

class ConfirmPhoneNumbersPageSpec extends BasePageISpec {

  val path = "/confirm-new-telephone-numbers"
  val newLandline = "012345678910"
  val newMobile = "019876543210"

  "Calling the Confirm Phone Numbers (.show) route" when {

    def show(sessionKeys: (String, String)*): WSResponse = get(path, Map(sessionKeys: _*))

    "the user is a authenticated" when {

      "there is a new landline in the session" should {

        "load successfully" in {

          given.user.isAuthenticated

          val result = show(SessionKeys.prepopulationLandlineKey -> newLandline)

          result should have(
            httpStatus(Status.OK),
            pageTitle(messages("confirmPhoneNumbers.title"))
          )
        }
      }

      "there is a new mobile in the session" should {

        "load successfully" in {

          given.user.isAuthenticated

          val result = show(SessionKeys.prepopulationMobileKey -> newMobile)

          result should have(
            httpStatus(Status.OK),
            pageTitle(messages("confirmPhoneNumbers.title"))
          )
        }
      }

      "there is a new landline and mobile in the session" should {

        "load successfully" in {

          given.user.isAuthenticated

          val result = show(SessionKeys.prepopulationLandlineKey -> newLandline, SessionKeys.prepopulationMobileKey -> newMobile)

          result should have(
            httpStatus(Status.OK),
            pageTitle(messages("confirmPhoneNumbers.title"))
          )
        }
      }
    }
  }
  "Calling the Update Phone Numbers route" when {

    def submit(sessionKeys: (String, String)*): WSResponse = post(path, Map(sessionKeys: _*))(Map.empty)

    "the landline is updated" should {

      "redirect to the confirmation screen" in {

        given.user.isAuthenticated

        VatSubscriptionStub.stubCustomerInfo
        VatSubscriptionStub.stubUpdatePPOB

        val result = submit(SessionKeys.prepopulationLandlineKey -> newLandline)

        result should have(
          httpStatus(Status.SEE_OTHER),
          redirectURI(controllers.contactNumbers.routes.ConfirmPhoneNumbersController.show().url)

        )

      }
    }

    "the mobile is updated" should {

      "redirect to the confirmation screen" in {

        given.user.isAuthenticated

        VatSubscriptionStub.stubCustomerInfo
        VatSubscriptionStub.stubUpdatePPOB

        val result = submit(SessionKeys.prepopulationMobileKey -> newMobile)

        result should have(
          httpStatus(Status.SEE_OTHER),
          redirectURI(controllers.contactNumbers.routes.ConfirmPhoneNumbersController.show().url)

        )

      }
    }

    "the landline and mobile are updated" should {

      "redirect to the confirmation screen" in {

        given.user.isAuthenticated

        VatSubscriptionStub.stubCustomerInfo
        VatSubscriptionStub.stubUpdatePPOB

        val result = submit(SessionKeys.prepopulationLandlineKey -> newLandline, SessionKeys.prepopulationMobileKey -> newMobile)

        result should have(
          httpStatus(Status.SEE_OTHER),
          redirectURI(controllers.contactNumbers.routes.ConfirmPhoneNumbersController.show().url)

        )

      }
    }
  }
}
