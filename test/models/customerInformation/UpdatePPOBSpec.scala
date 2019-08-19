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

package models.customerInformation

import assets.CustomerInfoConstants._
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class UpdatePPOBSpec extends UnitSpec {

  "UpdatePPOB" when {

    "all optional fields are present" should {

      "parse to JSON correctly" in {
        Json.toJson(fullUpdatePPOBModel) shouldBe fullUpdatePPOBJson
      }
    }

    "no optional fields are present" should {

      "parse to JSON correctly" in {
        Json.toJson(minUpdatePPOBModel) shouldBe minPPOBJson
      }
    }
  }
}

