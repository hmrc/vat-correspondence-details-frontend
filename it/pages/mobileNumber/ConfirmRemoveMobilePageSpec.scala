/*
 * Copyright 2020 HM Revenue & Customs
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
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse

class ConfirmRemoveMobilePageSpec extends BasePageISpec {

  val path = "/confirm-remove-mobile-number"
  val testValidationMobile: String = "07446033333"

  "Calling the Confirm Remove Mobile route" should {

    def show: WSResponse =
      get(path, Map(validationMobileKey -> testValidationMobile) ++ formatInflightChange(Some("false")))

    "load successfully" in {

      given.user.isAuthenticated
      val result = show
      result should have(
        httpStatus(Status.OK),
        pageTitle(generateDocumentTitle("confirmRemoveMobile.title"))
      )
    }
  }
}
