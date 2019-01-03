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
import play.api.libs.json._

case class PPOB(address: PPOBAddress,
                contactDetails: Option[ContactDetails],
                websiteAddress: Option[String])

object PPOB {

  private val addressPath = JsPath \ "address"
  private val contactDetailsPath = JsPath \ "contactDetails"
  private val websiteAddressPath = JsPath \ "websiteAddress"

  implicit val reads: Reads[PPOB] = (
    addressPath.read[PPOBAddress] and
    contactDetailsPath.readNullable[ContactDetails].orElse(Reads.pure(None)) and
    websiteAddressPath.readNullable[String].orElse(Reads.pure(None))
  )(PPOB.apply _)

  implicit val writes: Writes[PPOB] = (
    addressPath.write[PPOBAddress] and
    contactDetailsPath.writeNullable[ContactDetails] and
    websiteAddressPath.writeNullable[String]
  )(unlift(PPOB.unapply))
}
