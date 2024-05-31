/*
 * Copyright 2024 HM Revenue & Customs
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

import common.SessionKeys.prepopulationWebsiteKey
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse

class ConfirmWebsitePageSpec extends BasePageISpec {

  val path = "/confirm-new-website-address"
  val newWebsite = "www.coke.biz"

  "Calling the Confirm Website (.show) route" when {

    def show: WSResponse = get(path, Map(prepopulationWebsiteKey -> newWebsite) ++ formatInflightChange(Some("false")))

    "the user is a authenticated" when {

      "there is a new website in the session" should {

        "load successfully" in {

          given.user.isAuthenticated

          val result = show

          result should have(
            httpStatus(Status.OK),
            pageTitle(generateDocumentTitle("checkYourAnswers.title"))
          )
        }
      }
    }
  }
}
