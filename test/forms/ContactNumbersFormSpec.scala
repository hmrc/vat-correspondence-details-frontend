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

package forms

import forms.ContactNumbersForm._
import models.customerInformation.ContactNumbers
import play.api.data.FormError
import utils.TestUtil

class ContactNumbersFormSpec extends TestUtil {

  "The contact numbers form" should {

    val testLandline = "01952123456"
    val testMobile = "07890123456"

    "construct a model given a valid landline and mobile" when {
      "the contact numbers are from GB" when {
        "the numbers use no delimiters" when {
          "the country code is not supplied" in {
            val actual = contactNumbersForm("", "").bind(Map("landlineNumber" -> testLandline, "mobileNumber" -> testMobile))
            actual.value shouldBe Some(ContactNumbers(Some(testLandline), Some(testMobile)))
          }


          "the country code is supplied" in {
            val actual = contactNumbersForm("", "")
              .bind(Map("landlineNumber" -> ("+44" + testLandline.drop(1)), "mobileNumber" -> ("+44" + testMobile.drop(1))))
            actual.value shouldBe Some(ContactNumbers(Some("+44" + testLandline.drop(1)), Some("+44" + testMobile.drop(1))))
          }
        }

        "the numbers use space as a delimiter" when {
          "the country code is not supplied" in {
            val landline = "01952 123 456"
            val mobile = "078 901 234 56"
            val actual = contactNumbersForm("", "").bind(Map("landlineNumber" -> landline, "mobileNumber" -> mobile))
            actual.value shouldBe Some(ContactNumbers(Some(landline), Some(mobile)))
          }


          "the country code is supplied" in {
            val landline = "+441952 123 456"
            val mobile = "+4478 901 234 56"
            val actual = contactNumbersForm("", "").bind(Map("landlineNumber" -> landline, "mobileNumber" -> mobile))
            actual.value shouldBe Some(ContactNumbers(Some(landline), Some(mobile)))
          }
        }

        "the numbers use - as a delimiter" when {
          "the country code is not supplied" in {
            val landline = "01952-123-456"
            val mobile = "078-901-234-56"
            val actual = contactNumbersForm("", "").bind(Map("landlineNumber" -> landline, "mobileNumber" -> mobile))
            actual.value shouldBe Some(ContactNumbers(Some(landline), Some(mobile)))
          }

          "the country code is supplied" in {
            val landline = "+441952-123-456"
            val mobile = "+4478-901-234-56"
            val actual = contactNumbersForm("", "").bind(Map("landlineNumber" -> landline, "mobileNumber" -> mobile))
            actual.value shouldBe Some(ContactNumbers(Some(landline), Some(mobile)))
          }
        }
      }

      "the contact numbers are from a non GB country code" when {
        "the numbers use no delimiters" in {
          val esLandline = "+34912345678"
          val esMobile = "+34912345679"
          val actual = contactNumbersForm("", "").bind(Map("landlineNumber" -> esLandline, "mobileNumber" -> esMobile))
          actual.value shouldBe Some(ContactNumbers(Some(esLandline), Some(esMobile)))
        }

        "the numbers use space as a delimiters" in {
          val usLandline = "+1 332 555 0100"
          val usMobile = "+1 332 555 0101"
          val actual = contactNumbersForm("", "").bind(Map("landlineNumber" -> usLandline, "mobileNumber" -> usMobile))
          actual.value shouldBe Some(ContactNumbers(Some(usLandline), Some(usMobile)))
        }

        "the numbers use - as a delimiters" in {
          val gerLandline = "+49-2345-55776"
          val gerMobile = "+49-2345-55777"
          val actual = contactNumbersForm("", "").bind(Map("landlineNumber" -> gerLandline, "mobileNumber" -> gerMobile))
          actual.value shouldBe Some(ContactNumbers(Some(gerLandline), Some(gerMobile)))
        }
      }
    }

    "throw a validation error" when {
      "both phone numbers are empty" in {
        val formWithError = contactNumbersForm("", "").bind(Map("landlineNumber" -> "", "mobileNumber" -> ""))
        formWithError.errors should contain(FormError("", "captureContactNumbers.error.noEntry"))
      }

      "one phone number is supplied but is not a recognised phone number" in {
        val formWithError = contactNumbersForm("", "").bind(Map("landlineNumber" -> "+441231231231231", "mobileNumber" -> ""))
        formWithError.errors should contain(FormError("landlineNumber", "captureContactNumbers.error.invalid"))
      }

      "one phone number is supplied but has invalid characters" in {
        val formWithError = contactNumbersForm("", "").bind(Map("landlineNumber" -> "", "mobileNumber" -> "01952@123@456"))
        formWithError.errors should contain(FormError("mobileNumber", "captureContactNumbers.error.invalid"))
      }
    }
  }
}
