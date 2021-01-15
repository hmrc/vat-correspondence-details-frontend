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

import assets.BaseTestConstants.{testValidationMobile, testPrepopMobile}
import forms.MobileNumberForm.mobileNumberForm
import play.api.data.FormError
import utils.TestUtil

class MobileNumberFormSpec extends TestUtil {

  "The mobile number form" should {

    "successfully bind when a valid mobile number is provided" in {
      val result = mobileNumberForm(testValidationMobile).bind(Map("mobileNumber" -> testPrepopMobile))
      result.value shouldBe Some(testPrepopMobile)
    }

    "fail to bind" when {

      "the mobile number has not been changed" in {
        val result = mobileNumberForm(testValidationMobile).bind(Map("mobileNumber" -> testValidationMobile))
        result.value shouldBe None
        result.errors should contain(FormError("mobileNumber", "captureMobile.error.notChanged"))
      }

      "the mobile number exceeds the max length" in {
        val numberTooLong = "0777 1111 111 111 111 111"
        val result = mobileNumberForm(testValidationMobile).bind(Map("mobileNumber" -> numberTooLong))
        result.value shouldBe None
        result.errors should contain(FormError("mobileNumber", "captureMobile.error.invalid"))
      }

      "the mobile number has invalid characters" in {
        val result = mobileNumberForm(testValidationMobile).bind(Map("mobileNumber" -> (testValidationMobile + "§")))
        result.value shouldBe None
        result.errors should contain(FormError("mobileNumber", "captureMobile.error.invalid"))
      }
    }
  }
}
