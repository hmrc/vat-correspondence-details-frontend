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

package forms

import assets.BaseTestConstants.{testPrepopLandline, testValidationLandline}
import forms.LandlineNumberForm._
import org.scalatest.matchers.should.Matchers
import play.api.data.FormError
import utils.TestUtil

class LandlineNumberFormSpec extends TestUtil with Matchers {

  "The landline number form" should {

    "successfully bind when a valid number is provided" in {
      val result = landlineNumberForm(testValidationLandline).bind(Map("landlineNumber" -> testPrepopLandline))
      result.value shouldBe Some(testPrepopLandline)
    }

    "fail to bind" when {

      "the landline number has not been changed" in {
        val result = landlineNumberForm(testValidationLandline).bind(Map("landlineNumber" -> testValidationLandline))
        result.value shouldBe None
        result.errors should contain(FormError("landlineNumber", "captureLandline.error.notChanged"))
      }

      "the landline number exceeds the max length" in {
        val numberTooLong = "0161 1111 111 111 111 111"
        val result = landlineNumberForm(testValidationLandline).bind(Map("landlineNumber" -> numberTooLong))
        result.value shouldBe None
        result.errors should contain(FormError("landlineNumber", "captureLandline.error.invalid"))
      }

      "the landline number has invalid characters" in {
        val result = landlineNumberForm(testValidationLandline).bind(Map("landlineNumber" -> (testValidationLandline + "§")))
        result.value shouldBe None
        result.errors should contain(FormError("landlineNumber", "captureLandline.error.invalid"))
      }
    }
  }
}
