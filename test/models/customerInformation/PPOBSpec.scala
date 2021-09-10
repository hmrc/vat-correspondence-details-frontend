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

package models.customerInformation

import assets.CustomerInfoConstants._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.Json

class PPOBSpec extends AnyWordSpecLike with Matchers {

  "PPOB" should {

    "parse from Json" when {

      "all fields are present" in {
        val result = fullPPOBJson.as[PPOB]
        result shouldBe fullPPOBModel
      }

      "the minimum number of fields are present" in {
        val result = minPPOBJson.as[PPOB]
        result shouldBe minPPOBModel
      }
    }

    "parse to Json" when {

      "all fields are present" in {
        val result = Json.toJson(fullPPOBModel)
        result shouldBe fullPPOBJson
      }

      "the minimum number of fields are present" in {
        val result = Json.toJson(minPPOBModel)
        result shouldBe minPPOBJson
      }
    }
  }
}
