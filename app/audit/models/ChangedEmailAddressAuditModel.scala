/*
 * Copyright 2022 HM Revenue & Customs
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


case class ChangedEmailAddressAuditModel(currentEmailAddress: Option[String],
                                         requestedEmailAddress: String,
                                         vrn: String,
                                         isAgent: Boolean,
                                         arn: Option[String]) extends AuditModel {

  override val auditType: String = "ChangeEmailAddress"
  override val detail: JsValue = Json.toJson(this)
  override val transactionName: String = "change-vat-email-address"

}

object ChangedEmailAddressAuditModel extends JsonObjectSugar {

  implicit val writes: Writes[ChangedEmailAddressAuditModel] = Writes { model =>
    jsonObjNoNulls(
      "isAgent" -> model.isAgent,
      "arn" -> model.arn,
      "vrn" -> model.vrn,
      "currentEmailAddress" -> model.currentEmailAddress,
      "requestedEmailAddress" -> model.requestedEmailAddress
    )
  }
}
