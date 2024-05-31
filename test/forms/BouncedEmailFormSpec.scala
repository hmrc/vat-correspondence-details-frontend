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

package forms

import models.customerInformation.{Add, Verify, VerifyAdd}
import org.scalatest.matchers.should.Matchers
import play.api.i18n.Messages
import utils.TestUtil

class BouncedEmailFormSpec extends TestUtil with Matchers {

  "Binding a form with valid data for verifying the email address" should {

    val data = Map(VerifyAdd.id -> Verify.value)
    val form = BouncedEmailForm.bouncedEmailForm.bind(data)

    "result in a form with no errors" in {
      form.hasErrors shouldBe false
    }

    "generate the correct model" in {
      form.value shouldBe Some(Verify)
    }
  }

  "Binding a form with valid data for adding another email address" should {

    val data = Map(VerifyAdd.id -> Add.value)
    val form = BouncedEmailForm.bouncedEmailForm.bind(data)

    "result in a form with no errors" in {
      form.hasErrors shouldBe false
    }

    "generate the correct model" in {
      form.value shouldBe Some(Add)
    }
  }

  "Binding a form with invalid data" when {

    "no option has been selected" should {

      val missingOption: Map[String, String] = Map.empty
      val form = BouncedEmailForm.bouncedEmailForm.bind(missingOption)

      "result in a form with errors" in {
        form.hasErrors shouldBe true
      }

      "throw the correct error message" in {
        Messages(form.errors.head.message) shouldBe "Choose an option"
      }
    }
  }

  "A form built from a valid model" when {

    "verifying the email address" should {

      "generate the correct mapping" in {
        val form = BouncedEmailForm.bouncedEmailForm.fill(Verify)
        form.data shouldBe Map(VerifyAdd.id -> Verify.value)
      }
    }

    "adding a new email address to replace the unverified one" should {

      "generate the correct mapping" in {
        val form = BouncedEmailForm.bouncedEmailForm.fill(Add)
        form.data shouldBe Map(VerifyAdd.id -> Add.value)
      }
    }
  }

}
