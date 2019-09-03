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

package models.customerInformation

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CustomerInformation(ppob: PPOB,
                               pendingChanges: Option[PendingChanges],
                               firstName: Option[String],
                               lastName: Option[String],
                               organisationName: Option[String],
                               tradingName: Option[String]) {

  val approvedAddress: PPOBAddress = ppob.address
  val pendingAddress: Option[PPOBAddress] = pendingChanges.flatMap(_.ppob.map(_.address))

  val approvedEmail: Option[String] = ppob.contactDetails.flatMap(_.emailAddress)
  val pendingEmail: Option[String] = pendingChanges.flatMap(_.ppob.flatMap(_.contactDetails.flatMap(_.emailAddress)))

  val approvedLandline: Option[String] = ppob.contactDetails.flatMap(_.phoneNumber)
  val approvedMobile: Option[String] = ppob.contactDetails.flatMap(_.mobileNumber)
  val pendingLandline: Option[String] = pendingChanges.flatMap(_.ppob.flatMap(_.contactDetails.flatMap(_.phoneNumber)))
  val pendingMobile: Option[String] = pendingChanges.flatMap(_.ppob.flatMap(_.contactDetails.flatMap(_.mobileNumber)))

  val approvedWebsite: Option[String] = ppob.websiteAddress
  val pendingWebsite: Option[String] = pendingChanges.flatMap(_.ppob.flatMap(_.websiteAddress))

  val sameAddress: Boolean = pendingAddress.fold(false)(_ == approvedAddress)
  val sameEmail: Boolean = approvedEmail == pendingEmail
  val sameLandline: Boolean = approvedLandline == pendingLandline
  val sameMobile: Boolean = approvedMobile == pendingMobile
  val sameWebsite: Boolean = approvedWebsite == pendingWebsite

  def entityName: Option[String] =
    (firstName, lastName, tradingName, organisationName) match {
      case (Some(first), Some(last), None, None) => Some(s"$first $last")
      case (None, None, None, orgName) => orgName
      case _ => tradingName
    }
}

object CustomerInformation {

  private val ppobPath = JsPath \ "ppob"
  private val pendingChangesPath = JsPath \ "pendingChanges"
  private val firstNamePath = JsPath \ "customerDetails" \ "firstName"
  private val lastNamePath = JsPath \ "customerDetails" \ "lastName"
  private val organisationNamePath = JsPath \ "customerDetails" \ "organisationName"
  private val tradingNamePath = JsPath \ "customerDetails" \ "tradingName"

  implicit val reads: Reads[CustomerInformation] = (
    ppobPath.read[PPOB] and
    pendingChangesPath.readNullable[PendingChanges].orElse(Reads.pure(None)) and
    firstNamePath.readNullable[String].orElse(Reads.pure(None)) and
    lastNamePath.readNullable[String].orElse(Reads.pure(None)) and
    organisationNamePath.readNullable[String].orElse(Reads.pure(None)) and
    tradingNamePath.readNullable[String].orElse(Reads.pure(None))
  )(CustomerInformation.apply _)
}
