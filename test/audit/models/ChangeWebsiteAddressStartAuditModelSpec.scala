/*
 * Copyright 2023 HM Revenue & Customs
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

import assets.BaseTestConstants.{arn, testWebsite, vrn}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsValue, Json}

class ChangeWebsiteAddressStartAuditModelSpec extends AnyWordSpecLike with Matchers {

  "ChangeWebsiteAddressStartAuditModel" when {

    "the user is not an agent" should {

      val model = ChangeWebsiteAddressStartAuditModel(Some(testWebsite), vrn, None)
      val expectedJson: JsValue = Json.obj(
        "isAgent" -> false,
        "vrn" -> vrn,
        "currentWebsiteAddress" -> testWebsite
      )

      "generate the correct audit detail" in {
        model.detail shouldBe expectedJson
      }
    }

    "the user is an agent" should {

      val model = ChangeWebsiteAddressStartAuditModel(Some(testWebsite), vrn, Some(arn))
      val expectedJson: JsValue = Json.obj(
        "isAgent" -> true,
        "arn" -> arn,
        "vrn" -> vrn,
        "currentWebsiteAddress" -> testWebsite
      )

      "generate the correct audit detail" in {
        model.detail shouldBe expectedJson
      }
    }

    "the user does not have a current website" should {

      val model = ChangeWebsiteAddressStartAuditModel(None, vrn, None)
      val expectedJson: JsValue = Json.obj(
        "isAgent" -> false,
        "vrn" -> vrn
      )

      "generate the correct audit detail" in {
        model.detail shouldBe expectedJson
      }
    }
  }
}
