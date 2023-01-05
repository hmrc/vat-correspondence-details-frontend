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
import pages.BasePageISpec
import play.api.http.Status
import play.api.libs.ws.WSResponse

class ConfirmRemoveLandlinePageSpec extends BasePageISpec {

  val path = "/confirm-remove-landline-number"
  val testValidationLandline: String = "01952123456"

  "Calling the Confirm Remove Landline route" should {

    def show: WSResponse =
      get(path, Map(validationLandlineKey -> testValidationLandline) ++ formatInflightChange(Some("false")))

    "load successfully" in {

      given.user.isAuthenticated
      val result = show
      result should have(
        httpStatus(Status.OK),
        pageTitle(generateDocumentTitle("confirmRemoveLandline.title"))
      )
    }
  }
}
