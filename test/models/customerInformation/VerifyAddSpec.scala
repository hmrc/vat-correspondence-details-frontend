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

package models.customerInformation

import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json
import utils.TestUtil

class VerifyAddSpec extends TestUtil with Matchers {

  "VerifyAdd.Verify" should {

    "serialize to the correct JSON" in {
      Json.toJson(Verify) shouldBe Json.obj(VerifyAdd.id -> Verify.value)
    }

    "deserialize from the correct JSON" in {
      Json.obj(VerifyAdd.id -> Verify.value).as[VerifyAdd] shouldBe Verify
    }
  }

  "VerifyAdd.Add" should {

    "serialize to the correct JSON" in {
      Json.toJson(Add) shouldBe Json.obj(VerifyAdd.id -> Add.value)
    }

    "deserialize from the correct JSON" in {
      Json.obj(VerifyAdd.id -> Add.value).as[VerifyAdd] shouldBe Add
    }
  }
}
