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

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads, Writes}

case class PPOBAddress(line1: String,
                       line2: Option[String],
                       line3: Option[String],
                       line4: Option[String],
                       line5: Option[String],
                       postCode: Option[String],
                       countryCode: String)

object PPOBAddress {

  private val line1Path = JsPath \ "line1"
  private val line2Path = JsPath \ "line2"
  private val line3Path = JsPath \ "line3"
  private val line4Path = JsPath \ "line4"
  private val line5Path = JsPath \ "line5"
  private val postCodePath = JsPath \ "postCode"
  private val countryCodePath = JsPath \ "countryCode"

  implicit val reads: Reads[PPOBAddress] = (
    line1Path.read[String] and
    line2Path.readNullable[String] and
    line3Path.readNullable[String] and
    line4Path.readNullable[String] and
    line5Path.readNullable[String] and
    postCodePath.readNullable[String] and
    countryCodePath.read[String]
  ) (PPOBAddress.apply _)

  implicit val writes: Writes[PPOBAddress] = (
    line1Path.write[String] and
    line2Path.writeNullable[String] and
    line3Path.writeNullable[String] and
    line4Path.writeNullable[String] and
    line5Path.writeNullable[String] and
    postCodePath.writeNullable[String] and
    countryCodePath.write[String]
  ) (unlift(PPOBAddress.unapply))
}
