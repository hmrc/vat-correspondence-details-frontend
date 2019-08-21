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

package pages.website

import common.SessionKeys.{prepopulationWebsiteKey, validationWebsiteKey}
import forms.WebsiteForm
import helpers.SessionCookieCrumbler
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse
import stubs.VatSubscriptionStub

class CaptureWebsitePageSpec extends BasePageISpec {

  val path = "/new-website-address"
  val currentWebsite = "www.pepsi.biz"
  val newWebsite = "www.coke.biz"

  "Calling the Capture Website (.show) route" when {

    def show: WSResponse = get(path, formatInflightChange(Some("false")))

    "the user is a authenticated" when {

      "a success response with a website is received from Vat Subscription" should {

        "load successfully" in {

          given.user.isAuthenticated

          VatSubscriptionStub.stubCustomerInfo

          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(generateDocumentTitle("captureWebsite.title"))
          )
        }

        "add the existing website to the session" in {

          given.user.isAuthenticated

          VatSubscriptionStub.stubCustomerInfo

          val result = show

          SessionCookieCrumbler.getSessionMap(result).get(validationWebsiteKey) shouldBe Some(currentWebsite)
        }
      }
    }
  }

  "Calling the Capture Website (.submit) route" when {

    def submit(data: String): WSResponse = post(path,
      Map(validationWebsiteKey -> currentWebsite)
        ++ formatInflightChange(Some("false"))
    )(toFormData(WebsiteForm.websiteForm(currentWebsite), data))

    "the user is authenticated" when {

      "a valid website address is submitted" should {

        "redirect to the the Confirm Website page" in {

          given.user.isAuthenticated

          VatSubscriptionStub.stubCustomerInfo

          val result = submit(newWebsite)

          result should have(
            httpStatus(Status.SEE_OTHER),
            redirectURI(controllers.website.routes.ConfirmWebsiteController.show().url)
          )
        }

        "add the existing website to the session" in {

          given.user.isAuthenticated

          VatSubscriptionStub.stubCustomerInfo

          val result = submit(newWebsite)

          SessionCookieCrumbler.getSessionMap(result).get(prepopulationWebsiteKey) shouldBe Some(newWebsite)
        }
      }
    }
  }
}
