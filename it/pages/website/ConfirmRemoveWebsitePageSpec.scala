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

import common.SessionKeys.validationWebsiteKey
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse

class ConfirmRemoveWebsitePageSpec extends BasePageISpec {

  val path = "/remove-website-address"
  val currentWebsite = "www.pepsi.biz"

  "Calling the Confirm Remove Website (.show) route" when {

    def show: WSResponse = get(path, Map(validationWebsiteKey -> currentWebsite) ++ formatInflightChange(Some("false")))

    "the user is a authenticated" when {

      "there is an existing website in the session" should {

        "load successfully" in {

          given.user.isAuthenticated

          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(generateDocumentTitle("confirmWebsiteRemove.title"))
          )
        }
      }
    }
  }
}
