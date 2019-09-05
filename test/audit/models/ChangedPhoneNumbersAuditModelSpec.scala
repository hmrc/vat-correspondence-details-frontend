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

package audit.models

import assets.BaseTestConstants._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec

class ChangedPhoneNumbersAuditModelSpec extends UnitSpec {

  "ChangedPhoneNumbersAuditModel" when {

    "the user is not an agent" should {

      val model = ChangedPhoneNumbersAuditModel(
        Some(testValidationLandline),
        Some(testValidationMobile),
        Some(testPrepopLandline),
        Some(testPrepopMobile),
        vrn,
        isAgent = false,
        None
      )

      val expectedJson: JsValue = Json.obj(
        "isAgent" -> false,
        "vrn" -> vrn,
        "currentLandlineNumber" -> testValidationLandline,
        "currentMobileNumber" -> testValidationMobile,
        "requestedLandlineNumber" -> testPrepopLandline,
        "requestedMobileNumber" -> testPrepopMobile
      )

      "generate the correct audit detail" in {
        model.detail shouldBe expectedJson
      }
    }

    "the user is an agent" should {

      val model = ChangedPhoneNumbersAuditModel(
        Some(testValidationLandline),
        None,
        None,
        Some(testPrepopMobile),
        vrn,
        isAgent = true,
        Some(arn)
      )

      val expectedJson: JsValue = Json.obj(
        "isAgent" -> true,
        "arn" -> arn,
        "vrn" -> vrn,
        "currentLandlineNumber" -> testValidationLandline,
        "requestedMobileNumber" -> testPrepopMobile
      )

      "generate the correct audit detail" in {
        model.detail shouldBe expectedJson
      }
    }

    "the user has no existing phone numbers" should {

      val model = ChangedPhoneNumbersAuditModel(
        None,
        None,
        Some(testPrepopLandline),
        None,
        vrn,
        isAgent = false,
        None
      )

      val expectedJson: JsValue = Json.obj(
        "isAgent" -> false,
        "vrn" -> vrn,
        "requestedLandlineNumber" -> testPrepopLandline
      )

      "generate the correct audit detail" in {
        model.detail shouldBe expectedJson
      }
    }
  }
}
