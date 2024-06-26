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

package models.customerInformation

import assets.CustomerInfoConstants._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.Json

class ContactDetailsSpec extends AnyWordSpecLike with Matchers {

  "ContactDetails" should {

    "parse from JSON" when {

      "all fields are present" in {
        val result = fullContactDetailsJson.as[ContactDetails]
        result shouldBe fullContactDetailsModel
      }

      "the minimum number of fields are present" in {
        val result = minContactDetailsJson.as[ContactDetails]
        result shouldBe minContactDetailsModel
      }
    }

    "parse to JSON" when {

      "all fields are present" in {
        val result = Json.toJson(fullContactDetailsModel)
        result shouldBe fullContactDetailsJson
      }

      "the minimum number of fields are present" in {
        val result = Json.toJson(minContactDetailsModel)
        result shouldBe minContactDetailsJson
      }
    }
  }
}
