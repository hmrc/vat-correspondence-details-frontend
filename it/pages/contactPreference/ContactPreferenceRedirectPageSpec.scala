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

package pages.contactPreference

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get => wireMockGet, _}
import pages.BasePageISpec
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.Json

class ContactPreferenceRedirectPageSpec extends BasePageISpec {

  "Calling GET /contact-preference-redirect" should {

    "redirect to the change email journey page" when {

      "the user has a contact preference of PAPER" in {

        val customerJson = Json.stringify(Json.obj(
          "ppob" -> Json.obj(
            "address" -> Json.obj(
              "line1" -> "12 some street name",
              "countryCode" -> "UK"
            )
          ),
          "commsPreference" -> "PAPER"
        ))

        given.user.isAuthenticated
        wireMockServer.stubFor(
          wireMockGet(urlPathEqualTo("/vat-subscription/999999999/full-information"))
            .willReturn(aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(customerJson))
        )

        val result = get("/contact-preference-redirect", formatInflightChange(Some("false")))


        result should have(
          httpStatus(SEE_OTHER),
          redirectURI("/vat-through-software/account/correspondence/contact-preference-email")
        )
      }

    }

    "redirect to the change address journey" when {

      "the user has a contact preference of DIGITAL" in {

        val customerJson = Json.stringify(Json.obj(
          "ppob" -> Json.obj(
            "address" -> Json.obj(
              "line1" -> "12 some street name",
              "countryCode" -> "UK"
            )
          ),
          "commsPreference" -> "DIGITAL"
        ))

        given.user.isAuthenticated
        wireMockServer.stubFor(
          wireMockGet(urlPathEqualTo("/vat-subscription/999999999/full-information"))
            .willReturn(aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(customerJson))
        )

        val result = get("/contact-preference-redirect", formatInflightChange(Some("false")))


        result should have(
          httpStatus(SEE_OTHER),
          redirectURI("/vat-through-software/account/correspondence/contact-preference-letter")
        )
      }

    }

  }

}
