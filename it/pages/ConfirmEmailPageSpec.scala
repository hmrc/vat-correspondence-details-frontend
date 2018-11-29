/*
 * Copyright 2018 HM Revenue & Customs
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

package pages

import play.api.http.Status
import play.api.libs.ws.WSResponse
import stubs.VatSubscriptionStub

class ConfirmEmailPageSpec extends BasePageISpec {

  val path = "/confirm-email-address"
  val email = "test@test.com"

  "Calling the Capture email route with an authenticated user" should {

    def show: WSResponse = get(path, formatEmail(Some(email)))

    "when a success response is received for Customer Details" in {

      given.user.isAuthenticated

      And("a successful response for an individual is stubbed")
      VatSubscriptionStub.stubCustomerInfo

      When("the Confirm email page is called")
      val result = show

      result should have {
        httpStatus(Status.OK)
        pageTitle(messages("confirmEmail.title"))
      }
    }
  }
}
