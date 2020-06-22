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

package models

import play.api.libs.json.Json
import utils.TestUtil

class YesNoSpec extends TestUtil {

  "YesNo.Yes" should {

    "serialize to the correct JSON" in {
      Json.toJson(Yes) shouldBe Json.obj(YesNo.id -> Yes.value)
    }

    "deserialize from the correct JSON" in {
      Json.obj(YesNo.id -> Yes.value).as[YesNo] shouldBe Yes
    }
  }

  "YesNo.No" should {

    "serialize to the correct JSON" in {
      Json.toJson(No) shouldBe Json.obj(YesNo.id -> No.value)
    }

    "deserialize from the correct JSON" in {
      Json.obj(YesNo.id -> No.value).as[YesNo] shouldBe No
    }
  }
}