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

package models.customerInformation

import models.customerInformation.CustomerInformation.{allowedInsolvencyTypes, blockedInsolvencyTypes}
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class CustomerInformation(ppob: PPOB,
                               pendingChanges: Option[PendingChanges],
                               firstName: Option[String],
                               lastName: Option[String],
                               organisationName: Option[String],
                               tradingName: Option[String],
                               commsPreference: Option[String],
                               isInsolvent: Boolean,
                               continueToTrade: Option[Boolean],
                               insolvencyType: Option[String]) {

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

  val pendingPpobChanges: Boolean = pendingChanges.fold(false)(_.ppob.isDefined)

  def entityName: Option[String] =
    (firstName, lastName, tradingName, organisationName) match {
      case (Some(first), Some(last), None, None) => Some(s"$first $last")
      case (None, None, None, orgName) => orgName
      case _ => tradingName
    }

  val isInsolventWithoutAccess: Boolean = {
    if(isInsolvent) {
      insolvencyType match {
        case Some(insolvencyType) if allowedInsolvencyTypes.contains(insolvencyType) => false
        case Some(insolvencyType) if blockedInsolvencyTypes.contains(insolvencyType) => true
        case _ => continueToTrade.contains(false)
      }
    } else {
      false
    }
  }
}

object CustomerInformation {

  val allowedInsolvencyTypes: Seq[String] = Seq("07", "12", "13", "14")
  val blockedInsolvencyTypes: Seq[String] = Seq("08", "09", "10", "15")

  private val ppobPath = JsPath \ "ppob"
  private val pendingChangesPath = JsPath \ "pendingChanges"
  private val firstNamePath = JsPath \ "customerDetails" \ "firstName"
  private val lastNamePath = JsPath \ "customerDetails" \ "lastName"
  private val organisationNamePath = JsPath \ "customerDetails" \ "organisationName"
  private val tradingNamePath = JsPath \ "customerDetails" \ "tradingName"
  private val isInsolventPath = JsPath \ "customerDetails" \ "isInsolvent"
  private val continueToTradePath = JsPath \ "customerDetails" \ "continueToTrade"
  private val insolvencyTypePath = JsPath \ "customerDetails" \ "insolvencyType"
  private val commsPreferencePath = JsPath \ "commsPreference"

  implicit val reads: Reads[CustomerInformation] = (
    ppobPath.read[PPOB] and
    pendingChangesPath.readNullable[PendingChanges] and
    firstNamePath.readNullable[String] and
    lastNamePath.readNullable[String] and
    organisationNamePath.readNullable[String] and
    tradingNamePath.readNullable[String] and
    commsPreferencePath.readNullable[String] and
    isInsolventPath.read[Boolean] and
    continueToTradePath.readNullable[Boolean] and
    insolvencyTypePath.readNullable[String]
  )(CustomerInformation.apply _)
}
