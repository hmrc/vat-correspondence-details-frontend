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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsValue, Json}

class AttemptedEmailAddressAuditModelSpec extends AnyWordSpecLike with Matchers {

  val attemptedAddressNonAgentModel: AttemptedEmailAddressAuditModel = AttemptedEmailAddressAuditModel(
    currentEmailAddress = Some("testemail@test.com"),
    attemptedEmailAddress = "attemptedchange@test.com",
    vrn = "987654321",
    isAgent = false,
    arn = None
  )

  val attemptedAddressAgentModel: AttemptedEmailAddressAuditModel = AttemptedEmailAddressAuditModel(
    currentEmailAddress = Some("testemail@test.com"),
    attemptedEmailAddress = "attemptedchange@test.com",
    vrn = "987654321",
    isAgent = true,
    arn = Some("XAIT123456789")
  )

  val noCurrentEmailAddressModel: AttemptedEmailAddressAuditModel = AttemptedEmailAddressAuditModel(
    currentEmailAddress = None,
    attemptedEmailAddress = "attemptedchange@test.com",
    vrn = "987654321",
    isAgent = false,
    arn = None
  )


  "CurrentEmailAddressAuditModel" when {

    "the user is not an agent" should {

      "have generate the correct audit information" in {

        val expectedJson: JsValue =  Json.obj(
          "vrn" -> "987654321",
          "isAgent" -> false,
          "currentEmailAddress" -> "testemail@test.com",
          "attemptedEmailAddress" -> "attemptedchange@test.com"
        )

        attemptedAddressNonAgentModel.detail shouldBe expectedJson
      }
    }

    "the user is an agent" should {

      "have generate the correct audit information" in {

        val expectedJson: JsValue = Json.obj(
          "isAgent" -> true,
          "arn" -> "XAIT123456789",
          "vrn" -> "987654321",
          "currentEmailAddress" -> "testemail@test.com",
          "attemptedEmailAddress" -> "attemptedchange@test.com"
        )

        attemptedAddressAgentModel.detail shouldBe expectedJson
      }
    }

    "the user does not have a current email" should {
      "have generate the correct audit information" in {
        val expectedJson: JsValue = Json.obj(
          "vrn" -> "987654321",
          "isAgent" -> false,
          "attemptedEmailAddress" -> "attemptedchange@test.com"
        )

        noCurrentEmailAddressModel.detail shouldBe expectedJson
      }
    }
  }
}

