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

package stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import helpers.WireMockMethods
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{JsObject, Json}

object VatSubscriptionStub extends WireMockMethods {

  private val getCustomerInfoUri: String = "/vat-subscription/([0-9]+)/full-information"
  private val updatePPOBUri: String = "/vat-subscription/([0-9]+)/ppob"
  private val updateEmailUri: String = "/vat-subscription/([0-9]{9})/contact-preference/email"
  private val updateContactPref: String = "/vat-subscription/([0-9]+)/contact-preference"

  def stubCustomerInfo: StubMapping = {
    when(method = GET, uri = getCustomerInfoUri)
      .thenReturn(status = OK, body = customerInfoJson)
  }

  def stubCustomerInfoNoEmail: StubMapping = {
    when(method = GET, uri = getCustomerInfoUri)
      .thenReturn(status = OK, body = customerInfoNoEmail)
  }

  def stubCustomerInfoEmailUnverified: StubMapping = {
    when(method = GET, uri = getCustomerInfoUri)
      .thenReturn(status = OK, body = customerInfoEmailUnverified)
  }

  def stubCustomerInfoInvalidJson: StubMapping = {
    when(method = GET, uri = getCustomerInfoUri)
      .thenReturn(status = OK, body = emptyCustomerInfo)
  }

  def stubUpdatePPOB: StubMapping = {
    when(method = PUT, uri = updatePPOBUri)
      .thenReturn(status = OK, body = successfulUpdateJson)
  }

  def stubUpdateEmail(email: String): StubMapping = {
    when(method = PUT, uri = updateEmailUri, body = Some(
      Json.stringify(Json.obj("emailAddress" -> email))
    ))
      .thenReturn(status = OK, body = successfulUpdateJson)
  }

  def stubUpdateContactPreference: StubMapping = {
    when(method = PUT, uri = updateContactPref).thenReturn(status = OK, body = successfulUpdateJson)
  }

  def stubUpdatePPOBNoMessage: StubMapping = {
    when(method = PUT, uri = updatePPOBUri)
      .thenReturn(status = OK, body = updateEmptyResponse)
  }

  def stubUpdateEmailError(email: String): StubMapping = {
    when(method = PUT, uri = updateEmailUri, body = Some(Json.stringify(Json.obj("emailAddress" -> email))))
      .thenReturn(status = INTERNAL_SERVER_ERROR, body = Json.obj("ha" -> "noway"))
  }

  def stubCustomerInfoError: StubMapping = {
    when(method = GET, uri = getCustomerInfoUri)
      .thenReturn(status = INTERNAL_SERVER_ERROR, body = Json.obj("fail" -> "nope"))
  }

  def stubUpdatePPOBError: StubMapping = {
    when(method = PUT, uri = updatePPOBUri)
      .thenReturn(status = INTERNAL_SERVER_ERROR, body = Json.obj("fail" -> "nope"))
  }

  val currentLandline = "01952123456"
  val currentMobile = "07890123456"

  val customerInfoJson: JsObject = Json.obj(
    "ppob" -> Json.obj(
      "address" -> Json.obj(
        "line1" -> "firstLine",
        "countryCode" -> "codeOfMyCountry"
      ),
      "contactDetails" -> Json.obj(
        "emailAddress" -> "testemail@test.com",
        "emailVerified" -> true,
        "primaryPhoneNumber" -> currentLandline,
        "mobileNumber" -> currentMobile
      ),
      "websiteAddress" -> "www.pepsi.biz"
    ),
    "customerDetails" -> Json.obj(
      "isInsolvent" -> false,
      "continueToTrade" -> true
    ),
    "commsPreference" -> "DIGITAL"
  )

  val customerInfoNoEmail: JsObject = Json.obj(
    "ppob" -> Json.obj(
      "address" -> Json.obj(
        "line1" -> "firstLine",
        "countryCode" -> "codeOfMyCountry"
      )
    ),
    "customerDetails" -> Json.obj(
      "isInsolvent" -> false
    )
  )

  val customerInfoEmailUnverified: JsObject = Json.obj(
    "ppob" -> Json.obj(
      "address" -> Json.obj(
        "line1" -> "firstLine",
        "countryCode" -> "codeOfMyCountry"
      ),
      "contactDetails" -> Json.obj(
        "emailAddress" -> "testemail@test.com",
        "emailVerified" -> false,
        "primaryPhoneNumber" -> currentLandline,
        "mobileNumber" -> currentMobile
      ),
      "websiteAddress" -> "www.pepsi.biz"
    ),
    "customerDetails" -> Json.obj(
      "isInsolvent" -> false,
    )
  )

  val emptyCustomerInfo: JsObject = Json.obj("xxx" -> "xxx")

  val successfulUpdateJson: JsObject = Json.obj(
    "formBundle" -> "success"
  )

  val updateEmptyResponse: JsObject = Json.obj(
    "formBundle" -> ""
  )
}
