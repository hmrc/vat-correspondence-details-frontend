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

package assets

import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoError
import models.customerInformation.{ContactDetails, CustomerInformation, PPOB, PPOBAddress}
import play.api.http.Status
import play.api.libs.json.{JsObject, Json}

object CustomerInfoConstants {

  val fullPPOBAddressJson: JsObject = Json.obj(
    "line1" -> "firstLine",
    "line2" -> "secondLine",
    "line3" -> "thirdLine",
    "line4" -> "fourthLine",
    "line5" -> "fifthLine",
    "postCode" -> "codeOfMyPost",
    "countryCode" -> "codeOfMyCountry"
  )

  val fullPPOBAddressModel = PPOBAddress(
    "firstLine",
    Some("secondLine"),
    Some("thirdLine"),
    Some("fourthLine"),
    Some("fifthLine"),
    Some("codeOfMyPost"),
    "codeOfMyCountry"
  )

  val minPPOBAddressJson: JsObject = Json.obj(
    "line1" -> "firstLine",
    "countryCode" -> "codeOfMyCountry"
  )

  val minPPOBAddressModel = PPOBAddress(
    "firstLine",
    None,
    None,
    None,
    None,
    None,
    "codeOfMyCountry"
  )

  val fullContactDetailsJson: JsObject = Json.obj(
    "primaryPhoneNumber" -> "01234567890",
    "mobileNumber" -> "07707707707",
    "faxNumber" -> "0123456789",
    "emailAddress" -> "pepsimac@gmail.com",
    "emailVerified" -> true
  )

  val fullContactDetailsModel = ContactDetails(
    Some("01234567890"),
    Some("07707707707"),
    Some("0123456789"),
    Some("pepsimac@gmail.com"),
    Some(true)
  )

  val minContactDetailsJson: JsObject = Json.obj()

  val minContactDetailsModel = ContactDetails(
    None,
    None,
    None,
    None,
    None
  )

  val fullPPOBJson: JsObject = Json.obj(
    "address" -> fullPPOBAddressJson,
    "contactDetails" -> fullContactDetailsJson,
    "websiteAddress" -> "www.pepsi-mac.biz"
  )

  val fullPPOBModel = PPOB(
    fullPPOBAddressModel,
    Some(fullContactDetailsModel),
    Some("www.pepsi-mac.biz")
  )

  val minPPOBJson: JsObject = Json.obj(
    "address" -> minPPOBAddressJson
  )

  val minPPOBModel = PPOB(
    minPPOBAddressModel,
    None,
    None
  )

  val customerInfoJson: JsObject = Json.obj("ppob" -> fullPPOBJson)
  val customerInfoModel = CustomerInformation(fullPPOBModel)

  val invalidJsonError = GetCustomerInfoError(Status.INTERNAL_SERVER_ERROR, "The endpoint returned invalid JSON.")
}