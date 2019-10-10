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

package pages.email

import common.SessionKeys
import config.FrontendAppConfig
import pages.BasePageISpec
import play.api.http.Status
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.OK
import stubs.AuthStub.clientVRN
import stubs.ContactPreferencesStub

class EmailChangeSuccessPageSpec extends BasePageISpec {

  val successEmailPath = "/email-address-confirmation"

  val session: Map[String, String] = Map(SessionKeys.emailChangeSuccessful -> "true")

  "Calling the EmailChangeSuccessController.show method" when {

    "the user is authenticated and has the email change successful session key" when {

      def show: WSResponse = get(successEmailPath, session)

      "render the success email page" in {

        given.user.isAuthenticated

        And("a successful response for an individual is stubbed")
        ContactPreferencesStub.getContactPrefs(OK, Json.obj("preference" -> "DiGiTaL"))

        When("the Confirm email page is called")
        val result = show

        result should have(
          httpStatus(Status.OK),
          elementText("#preference-message")(Messages("changeSuccess.helpOne.digital"))
        )
      }
    }
  }
}
