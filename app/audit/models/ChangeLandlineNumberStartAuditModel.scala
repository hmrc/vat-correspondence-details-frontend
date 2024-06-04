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

package audit.models

import play.api.libs.json.{JsValue, Json, Writes}
import utils.JsonObjectSugar

case class ChangeLandlineNumberStartAuditModel(currentLandlineNumber: Option[String],
                                               vrn: String,
                                               arn: Option[String]) extends AuditModel {

  override val auditType: String = "ChangeLandlineNumberStart"
  override val detail: JsValue = Json.toJson(this)
  override val transactionName: String = "change-vat-landline-number"
}

object ChangeLandlineNumberStartAuditModel extends JsonObjectSugar {

  implicit val writes: Writes[ChangeLandlineNumberStartAuditModel] = Writes { model =>
    jsonObjNoNulls(
      "isAgent" -> model.arn.isDefined,
      "arn" -> model.arn,
      "vrn" -> model.vrn,
      "currentLandlineNumber" -> model.currentLandlineNumber
    )
  }
}

