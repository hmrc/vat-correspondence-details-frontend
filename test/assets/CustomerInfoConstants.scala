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

package assets

import models.contactPreferences.ContactPreference.{digital, paper}
import models.customerInformation._
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

  val fullPPOBAddressModel: PPOBAddress = PPOBAddress(
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

  val minPPOBAddressModel: PPOBAddress = PPOBAddress(
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

  val fullContactDetailsModel: ContactDetails = ContactDetails(
    Some("01234567890"),
    Some("07707707707"),
    Some("0123456789"),
    Some("pepsimac@gmail.com"),
    Some(true)
  )

  val contactDetailsUnverifiedEmail: ContactDetails = fullContactDetailsModel.copy(emailVerified = Some(false))

  val fullEmailAddressModel: EmailAddress = EmailAddress(
    Some("test@email.com"),
    Some(true)
  )

  val fullEmailAddressJson: JsObject = Json.obj(
    "emailAddress" -> "test@email.com",
    "emailVerified" -> true
  )

  val minEmailAddressModel: EmailAddress = EmailAddress(
    None,
    None
  )

  val minEmailAddressJson: JsObject = Json.obj()

  val minContactDetailsJson: JsObject = Json.obj()

  val minContactDetailsModel: ContactDetails = ContactDetails(
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

  val fullUpdatePPOBJson: JsObject = Json.obj(
    "address" -> fullPPOBAddressJson,
    "contactDetails" -> fullContactDetailsJson,
    "websiteAddress" -> "www.pepsi-mac.biz",
    "transactorOrCapacitorEmail" -> "test@test.com"
  )

  val fullPPOBModel: PPOB = PPOB(
    fullPPOBAddressModel,
    Some(fullContactDetailsModel),
    Some("www.pepsi-mac.biz")
  )

  val fullUpdatePPOBModel: UpdatePPOB = UpdatePPOB(
    fullPPOBAddressModel,
    Some(fullContactDetailsModel),
    Some("www.pepsi-mac.biz"),
    Some("test@test.com")
  )

  val minPPOBJson: JsObject = Json.obj(
    "address" -> minPPOBAddressJson
  )

  val minPPOBModel: PPOB = PPOB(
    minPPOBAddressModel,
    None,
    None
  )

  val minUpdatePPOBModel: UpdatePPOB = UpdatePPOB(
    minPPOBAddressModel,
    None,
    None,
    None
  )

  val pendingChangesModel: PendingChanges = PendingChanges(Some(fullPPOBModel), Some(digital))
  val pendingChangesJson: JsObject = Json.obj("PPOBDetails" -> fullPPOBModel, "commsPreference" -> digital)

  val fullCustomerInfoModel: CustomerInformation = CustomerInformation(
    fullPPOBModel,
    Some(pendingChangesModel),
    Some("Pepsi"),
    Some("Mac"),
    Some("PepsiMac Ltd"),
    Some("PepsiMac"),
    Some(digital)
  )

  val customerInfoEmailUnverified: CustomerInformation = fullCustomerInfoModel.copy(
    ppob = fullPPOBModel.copy(
      contactDetails = Some(fullContactDetailsModel.copy(
        emailVerified = Some(false)
      ))
    )
  )

  val minCustomerInfoModel: CustomerInformation = CustomerInformation(minPPOBModel, None, None, None, None, None, None)

  val customerInfoPendingAddressModel: CustomerInformation = fullCustomerInfoModel.copy(
    pendingChanges = Some(PendingChanges(Some(fullPPOBModel.copy(
      address = minPPOBAddressModel
    )), None))
  )

  val customerInfoPendingEmailModel: CustomerInformation = fullCustomerInfoModel.copy(
    pendingChanges = Some(PendingChanges(Some(fullPPOBModel.copy(
      contactDetails = Some(fullContactDetailsModel.copy(emailAddress = Some("myEmail@cool.com")))
    )), None))
  )

  val customerInfoPendingWebsiteModel: CustomerInformation = fullCustomerInfoModel.copy(
    pendingChanges = Some(PendingChanges(Some(fullPPOBModel.copy(
      websiteAddress = Some("www.new-website.co.uk")
    )), None))
  )

  val customerInfoPendingLandlineModel: CustomerInformation = fullCustomerInfoModel.copy(
    pendingChanges = Some(PendingChanges(Some(fullPPOBModel.copy(
      contactDetails = Some(fullContactDetailsModel.copy(phoneNumber = Some("01610111111")))
    )), None))
  )

  val customerInfoPendingMobileModel: CustomerInformation = fullCustomerInfoModel.copy(
    pendingChanges = Some(PendingChanges(Some(fullPPOBModel.copy(
      contactDetails = Some(fullContactDetailsModel.copy(mobileNumber = Some("07777777777")))
    )), None))
  )

  val customerInfoPendingContactPrefModel: CustomerInformation = fullCustomerInfoModel.copy(
    pendingChanges = Some(PendingChanges(None, Some(digital)))
  )

  val customerInfoPaperPrefModel: CustomerInformation = fullCustomerInfoModel.copy(commsPreference = Some(paper))

  val fullCustomerInfoJson: JsObject = Json.obj(
    "ppob" -> fullPPOBJson,
    "pendingChanges" -> pendingChangesJson,
    "customerDetails" -> Json.obj(
      "firstName" -> "Pepsi",
      "lastName" -> "Mac",
      "organisationName" -> "PepsiMac Ltd",
      "tradingName" -> "PepsiMac"
    ),
    "commsPreference" -> digital
  )
  val minCustomerInfoJson: JsObject = Json.obj("ppob" -> minPPOBJson)

  val invalidJsonError: ErrorModel = ErrorModel(Status.INTERNAL_SERVER_ERROR, "The endpoint returned invalid JSON.")
}
