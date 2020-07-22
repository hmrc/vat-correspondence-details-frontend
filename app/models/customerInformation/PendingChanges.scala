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
import play.api.libs.json.{JsPath, Reads}

case class PendingChanges(ppob: Option[PPOB],
                          commsPreference: Option[String])

object PendingChanges {

  private val ppobPath = JsPath \ "PPOBDetails"
  private val commsPreferencePath = JsPath \ "commsPreference"

  implicit val reads: Reads[PendingChanges] = (
    ppobPath.readNullable[PPOB] and
    commsPreferencePath.readNullable[String]
  )(PendingChanges.apply _)
}
