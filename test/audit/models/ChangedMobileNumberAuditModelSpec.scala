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

import assets.BaseTestConstants.{arn, testPrepopMobile, testValidationMobile, vrn}
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsValue, Json}
import utils.TestUtil

class ChangedMobileNumberAuditModelSpec extends TestUtil with Matchers {

  "ChangedMobileNumberAuditModel" when {

    "the user is not an agent" should {

      val model = ChangedMobileNumberAuditModel(
        Some(testValidationMobile),
        testPrepopMobile,
        vrn,
        isAgent = false,
        None
      )

      val expectedJson: JsValue = Json.obj(
        "isAgent" -> false,
        "vrn" -> vrn,
        "currentMobileNumber" -> testValidationMobile,
        "requestedMobileNumber" -> testPrepopMobile
      )

      "generate the correct audit detail" in {
        model.detail shouldBe expectedJson
      }
    }

    "the user is an agent" should {

      val model = ChangedMobileNumberAuditModel(
        Some(testValidationMobile),
        "",
        vrn,
        isAgent = true,
        Some(arn)
      )

      val expectedJson: JsValue = Json.obj(
        "isAgent" -> true,
        "arn" -> arn,
        "vrn" -> vrn,
        "currentMobileNumber" -> testValidationMobile,
        "requestedMobileNumber" -> ""
      )

      "generate the correct audit detail" in {
        model.detail shouldBe expectedJson
      }
    }

    "the user has no existing mobile number" should {

      val model = ChangedMobileNumberAuditModel(
        None,
        testPrepopMobile,
        vrn,
        isAgent = false,
        None
      )

      val expectedJson: JsValue = Json.obj(
        "isAgent" -> false,
        "vrn" -> vrn,
        "requestedMobileNumber" -> testPrepopMobile
      )

      "generate the correct audit detail" in {
        model.detail shouldBe expectedJson
      }
    }
  }
}
