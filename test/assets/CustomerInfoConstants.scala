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

package assets

import models.customerInformation.{ContactDetails, CustomerInformation, PPOB, PPOBAddress, _}
import models.errors.ErrorModel
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

  val fullEmailAddressModel = EmailAddress(
    Some("test@email.com"),
    Some(true)
  )

  val fullEmailAddressJson: JsObject = Json.obj(
    "emailAddress" -> "test@email.com",
    "emailVerified" -> true
  )

  val minEmailAddressModel = EmailAddress(
    None,
    None
  )

  val minEmailAddressJson: JsObject = Json.obj()

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

  val pendingChangesModel = PendingChanges(Some(minPPOBModel))
  val pendingChangesJson: JsObject = Json.obj("PPOBDetails" -> minPPOBModel)

  val customerInfoPendingAddressModel = CustomerInformation(
    fullPPOBModel,
    Some(PendingChanges(Some(PPOB(
      minPPOBAddressModel,
      Some(fullContactDetailsModel),
      Some("www.pepsi-mac.biz")
    )
  ))))

  val customerInfoPendingEmailModel = CustomerInformation(
    fullPPOBModel,
    Some(PendingChanges(Some(PPOB(
      fullPPOBAddressModel,
      Some(ContactDetails(None, None, None, Some("myEmail@cool.com"), Some(false))),
      None
    )))))

  val customerInfoPendingWebsiteModel = CustomerInformation(
    fullPPOBModel,
    Some(PendingChanges(Some(PPOB(
      fullPPOBAddressModel,
      Some(fullContactDetailsModel),
      Some("new email")
    )))))

  val fullCustomerInfoJson: JsObject = Json.obj(
    "ppob" -> fullPPOBJson,
    "pendingChanges" -> Some(PendingChanges(Some(fullPPOBModel)))
  )
  val minCustomerInfoJson: JsObject = Json.obj("ppob" -> minPPOBJson)

  val fullCustomerInfoModel = CustomerInformation(fullPPOBModel, Some(PendingChanges(Some(fullPPOBModel))))
  val minCustomerInfoModel = CustomerInformation(minPPOBModel, None)

  val invalidJsonError = ErrorModel(Status.INTERNAL_SERVER_ERROR, "The endpoint returned invalid JSON.")
}
