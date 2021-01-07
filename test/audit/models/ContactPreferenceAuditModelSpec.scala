/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec

class ContactPreferenceAuditModelSpec  extends UnitSpec {

    val contactPreferenceDigital: ContactPreferenceAuditModel = ContactPreferenceAuditModel(
      vrn = "987654321",
      contactPreference = "DIGITAL"
    )

    val contactPreferencePaper: ContactPreferenceAuditModel = ContactPreferenceAuditModel(
      vrn = "123456789",
      contactPreference = "PAPER"
    )

    "ContactPreferenceAuditModel" when {

      "the user has a digital preference" should {

        "have generate the correct audit information" in {

          val expectedJson: JsValue =  Json.obj(
            "vrn" -> "987654321",
            "contactPreference" -> "DIGITAL",
            "action" -> "ChangeEmailAddressContactPreference"
          )

          contactPreferenceDigital.detail shouldBe expectedJson
        }
      }

      "the user has a paper preference" should {

        "have generate the correct audit information" in {

          val expectedJson: JsValue =  Json.obj(
            "vrn" -> "123456789",
            "contactPreference" -> "PAPER",
            "action" -> "ChangeEmailAddressContactPreference"
          )

          contactPreferencePaper.detail shouldBe expectedJson
        }
      }
    }
  }


