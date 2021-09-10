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

import models.contactPreferences.ContactPreference
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.Json

class ChangedContactPrefEmailAuditModelSpec extends AnyWordSpecLike with Matchers {

  val auditModelWithEmail: ChangedContactPrefEmailAuditModel = ChangedContactPrefEmailAuditModel(
    Some("vim@test.com"),
    "nano@test.com",
    "999999999"
  )

  val auditModelNoEmail: ChangedContactPrefEmailAuditModel = auditModelWithEmail.copy(currentEmailAddress = None)

  "ChangedContactPrefEmailAuditModel" when {

    "there is a current email address" should {

      "audit the correct JSON detail" in {

        val expectedJson = Json.obj(
          "currentEmailAddress" -> "vim@test.com",
          "requestedEmailAddress" -> "nano@test.com",
          "currentContactPreference" -> ContactPreference.paper,
          "requestedContactPreference" -> ContactPreference.digital,
          "vrn" -> "999999999"
        )

        auditModelWithEmail.detail shouldBe expectedJson
      }
    }

    "there is not a current email address" should {

      "audit the correct JSON detail" in {

        val expectedJson = Json.obj(
          "requestedEmailAddress" -> "nano@test.com",
          "currentContactPreference" -> ContactPreference.paper,
          "requestedContactPreference" -> ContactPreference.digital,
          "vrn" -> "999999999"
        )

        auditModelNoEmail.detail shouldBe expectedJson
      }
    }
  }
}
