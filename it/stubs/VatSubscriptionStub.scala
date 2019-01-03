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

package stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import helpers.WireMockMethods
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{JsObject, Json}

object VatSubscriptionStub extends WireMockMethods {

  private val getCustomerInfoUri: String = "/vat-subscription/([0-9]+)/full-information"
  private val updateEmailUri: String = "/vat-subscription/([0-9]+)/email-address"

  def stubCustomerInfo: StubMapping = {
    when(method = GET, uri = getCustomerInfoUri)
      .thenReturn(status = OK, body = customerInfoJson)
  }

  def stubCustomerInfoNoEmailJson: StubMapping = {
    when(method = GET, uri = getCustomerInfoUri)
      .thenReturn(status = OK, body = emptyCustomerInfo)
  }

  def stubUpdateEmail: StubMapping = {
    when(method = PUT, uri = updateEmailUri)
      .thenReturn(status = OK, body = updateEmailJson)
  }

  def stubUpdateEmailNoMessage: StubMapping = {
    when(method = PUT, uri = updateEmailUri)
      .thenReturn(status = OK, body = updateEmailEmptyResponse)
  }

  def stubCustomerInfoError: StubMapping = {
    when(method = GET, uri = getCustomerInfoUri)
      .thenReturn(status = INTERNAL_SERVER_ERROR, body = Json.obj("fail" -> "nope"))
  }

  def stubUpdateEmailError: StubMapping = {
    when(method = PUT, uri = updateEmailUri)
      .thenReturn(status = INTERNAL_SERVER_ERROR, body = Json.obj("fail" -> "nope"))
  }

  val customerInfoJson: JsObject = Json.obj(
    "ppob" -> Json.obj(
      "address" -> Json.obj(
        "line1" -> "firstLine",
        "countryCode" -> "codeOfMyCountry"
      ),
      "contactDetails" -> Json.obj(
        "emailAddress" -> "testemail@test.com"
      ),
      "websiteAddress" -> "www.pepsi-mac.biz"
    )
  )

  val emptyCustomerInfo: JsObject = Json.obj("xxx" -> "xxx")

  val customerInfoNoEmailJson: JsObject = Json.obj(
    "ppob" -> Json.obj(
      "address" -> Json.obj(
        "line1" -> "firstLine",
        "countryCode" -> "codeOfMyCountry"
      ),
      "websiteAddress" -> "www.pepsi-mac.biz"
    )
  )

  val updateEmailJson: JsObject = Json.obj(
    "formBundle" -> "success"
  )

  val updateEmailEmptyResponse: JsObject = Json.obj(
    "formBundle" -> ""
  )
}
