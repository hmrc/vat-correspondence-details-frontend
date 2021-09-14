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

import assets.BaseTestConstants._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsValue, Json}

class ChangedWebsiteAddressAuditModelSpec extends AnyWordSpecLike with Matchers {

  "ChangedWebsiteAddressAuditModel" when {

    "the user is not an agent" should {

      val model = ChangedWebsiteAddressAuditModel(Some(testWebsite), testNewWebsite, vrn, isAgent = false, None)
      val expectedJson: JsValue = Json.obj(
        "isAgent" -> false,
        "vrn" -> vrn,
        "currentWebsiteAddress" -> testWebsite,
        "requestedWebsiteAddress" -> testNewWebsite
      )

      "generate the correct audit detail" in {
        model.detail shouldBe expectedJson
      }
    }

    "the user is an agent" should {

      val model = ChangedWebsiteAddressAuditModel(Some(testWebsite), testNewWebsite, vrn, isAgent = true, Some(arn))
      val expectedJson: JsValue = Json.obj(
        "isAgent" -> true,
        "arn" -> arn,
        "vrn" -> vrn,
        "currentWebsiteAddress" -> testWebsite,
        "requestedWebsiteAddress" -> testNewWebsite
      )

      "generate the correct audit detail" in {
        model.detail shouldBe expectedJson
      }
    }

    "the user does not have a current website" should {

      val model = ChangedWebsiteAddressAuditModel(None, testNewWebsite, vrn, isAgent = false, None)
      val expectedJson: JsValue = Json.obj(
        "isAgent" -> false,
        "vrn" -> vrn,
        "requestedWebsiteAddress" -> testNewWebsite
      )

      "generate the correct audit detail" in {
        model.detail shouldBe expectedJson
      }
    }
  }
}
