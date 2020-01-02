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

package models.customerInformation

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

case class UpdatePPOB(address: PPOBAddress,
                      contactDetails: Option[ContactDetails],
                      websiteAddress: Option[String],
                      transactorOrCapacitorEmail: Option[String])

object UpdatePPOB {

  private val addressPath = JsPath \ "address"
  private val contactDetailsPath = JsPath \ "contactDetails"
  private val websiteAddressPath = JsPath \ "websiteAddress"
  private val transactorOrCapacitorEmailPath = JsPath \ "transactorOrCapacitorEmail"

  implicit val writes: Writes[UpdatePPOB] = (
    addressPath.write[PPOBAddress] and
    contactDetailsPath.writeNullable[ContactDetails] and
    websiteAddressPath.writeNullable[String] and
    transactorOrCapacitorEmailPath.writeNullable[String]
  )(unlift(UpdatePPOB.unapply))
}
