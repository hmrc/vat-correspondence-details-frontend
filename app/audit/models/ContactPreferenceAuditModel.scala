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

package audit.models

import play.api.libs.json.{JsValue, Json, Writes}
import utils.JsonObjectSugar

case class ContactPreferenceAuditModel(vrn: String,
                                       contactPreference: String,
                                       action: String = "ChangeEmailAddressContactPreference") extends AuditModel {

  override val auditType: String = "ContactPreference"
  override val detail: JsValue = Json.toJson(this)
  override val transactionName: String = "contact-preference"

}

object ContactPreferenceAuditModel extends JsonObjectSugar {

  implicit val writes: Writes[ContactPreferenceAuditModel] = Writes { model =>
    jsonObjNoNulls(
      "vrn" -> model.vrn,
      "contactPreference" -> model.contactPreference,
      "action" -> model.action
    )
  }
}
