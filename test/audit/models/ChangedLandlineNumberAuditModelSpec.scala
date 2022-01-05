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

import assets.BaseTestConstants._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsValue, Json}

class ChangedLandlineNumberAuditModelSpec extends AnyWordSpecLike with Matchers {

  "ChangedLandlineNumberAuditModel" when {

    "the user is not an agent" should {

      val model = ChangedLandlineNumberAuditModel(
        Some(testValidationLandline),
        testPrepopLandline,
        vrn,
        isAgent = false,
        None
      )

      val expectedJson: JsValue = Json.obj(
        "isAgent" -> false,
        "vrn" -> vrn,
        "currentLandlineNumber" -> testValidationLandline,
        "requestedLandlineNumber" -> testPrepopLandline
      )

      "generate the correct audit detail" in {
        model.detail shouldBe expectedJson
      }
    }

    "the user is an agent" should {

      val model = ChangedLandlineNumberAuditModel(
        Some(testValidationLandline),
        "",
        vrn,
        isAgent = true,
        Some(arn)
      )

      val expectedJson: JsValue = Json.obj(
        "isAgent" -> true,
        "arn" -> arn,
        "vrn" -> vrn,
        "currentLandlineNumber" -> testValidationLandline,
        "requestedLandlineNumber" -> ""
      )

      "generate the correct audit detail" in {
        model.detail shouldBe expectedJson
      }
    }

    "the user has no existing landline number" should {

      val model = ChangedLandlineNumberAuditModel(
        None,
        testPrepopLandline,
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
