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

package forms

import utils.TestUtil

class PasscodeFormSpec extends TestUtil {

  "Binding the form with a six-character value" should {

    val data = Map("passcode" -> "ABCDEF")
    val form = PasscodeForm.form.bind(data)

    "result in a form with no errors" in {
      form.hasErrors shouldBe false
    }
  }

  "Binding the form with no data" should {

    val data = Map("passcode" -> "")
    val form = PasscodeForm.form.bind(data)

    "result in a form with errors" in {
      form.hasErrors shouldBe true
    }

    "contain one error" in {
      form.errors.size shouldBe 1
    }

    "have the correct error message key" in {
      form.errors.head.message shouldBe "passcode.error.empty"
    }
  }

  "Binding the form with too many characters" should {

    val data = Map("passcode" -> "TOOMANYCHARS")
    val form = PasscodeForm.form.bind(data)

    "result in a form with errors" in {
      form.hasErrors shouldBe true
    }

    "contain one error" in {
      form.errors.size shouldBe 1
    }

    "have the correct error message key" in {
      form.errors.head.message shouldBe "passcode.error.invalid"
    }
  }

  "Binding the form with too few characters" should {

    val data = Map("passcode" -> "NOPE")
    val form = PasscodeForm.form.bind(data)

    "result in a form with errors" in {
      form.hasErrors shouldBe true
    }

    "contain one error" in {
      form.errors.size shouldBe 1
    }

    "have the correct error message key" in {
      form.errors.head.message shouldBe "passcode.error.invalid"
    }
  }
}
