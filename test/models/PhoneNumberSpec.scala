/*
 * Copyright 2026 HM Revenue & Customs
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

import org.scalatest.matchers.should.Matchers
import utils.TestUtil

class PhoneNumberSpec extends TestUtil with Matchers {

  private val numberWithoutPrefixMinDigits = "1234567"
  private val numberWithoutPrefixMaxDigits = "12345678901234567890123"


  "isValid" should {

    "return true" when {

      "a local number is at the minimum valid length (8 chars: '0' + 7 digits)" in {
        val localNumberMinDigits = "0" + numberWithoutPrefixMinDigits
        PhoneNumber.isValid(localNumberMinDigits) shouldBe true
      }

      "a local number is at the maximum valid length (25 chars: '0' + 24 digits)" in {
        val localNumberMaxDigits = "0" + numberWithoutPrefixMaxDigits
        PhoneNumber.isValid(localNumberMaxDigits) shouldBe true
      }

      "a local number contains spaces that reduce to a valid stripped form" in {
        val localNumberWithSpaces = "01161 111 1111"
        PhoneNumber.isValid(localNumberWithSpaces) shouldBe true
      }

      "an international '+' number is at the minimum valid length (8 chars: '+' + 7 digits)" in {
        val intNumberWithPlusMinDigits = "+" + numberWithoutPrefixMinDigits
        PhoneNumber.isValid(intNumberWithPlusMinDigits) shouldBe true
      }

      "an international '+' number is at the maximum valid length (24 chars: '+' + 23 digits)" in {
        val intNumberWithPlusMaxDigits = "+" + numberWithoutPrefixMaxDigits
        PhoneNumber.isValid(intNumberWithPlusMaxDigits) shouldBe true
      }

      "an international '+' number contains spaces that reduce to a valid stripped form" in {
        val intNumberWithSpaces = "+44 7700 900 982"
        PhoneNumber.isValid(intNumberWithSpaces) shouldBe true
      }

      "an international '00' number is at the minimum valid length (9 chars: '00' + 7 digits)" in {
        val intNumberWithDoubleZeroMinDigits = "00" + numberWithoutPrefixMinDigits
        PhoneNumber.isValid(intNumberWithDoubleZeroMinDigits) shouldBe true
      }

      "an international '00' number is at the maximum valid length (25 chars: '00' + 23 digits)" in {
        val intNumberWithDoubleZeroMaxDigits = "00" + numberWithoutPrefixMaxDigits
        PhoneNumber.isValid(intNumberWithDoubleZeroMaxDigits) shouldBe true
      }

      "an international '00' number contains spaces that reduce to a valid stripped form" in {
        val intNumberWithSpaces = "0044 7700 900 982"
        PhoneNumber.isValid(intNumberWithSpaces) shouldBe true
      }

      "a UK phone number with '+44' prefix" in {
        val ukShortNumberIntPrefix = "+44" + numberWithoutPrefixMinDigits
        PhoneNumber.isValid(ukShortNumberIntPrefix) shouldBe true
      }

      "a UK phone number with '0044' prefix" in {
        val ukNumberWithDoubleZeroPrefix = "0044" + numberWithoutPrefixMinDigits
        PhoneNumber.isValid(ukNumberWithDoubleZeroPrefix) shouldBe true
      }
    }

    "return false" when {

      "a local number is one digit short of the minimum (7 chars: '0' + 6 digits)" in {
        val localNumberTooShort = "0123456"
        PhoneNumber.isValid(localNumberTooShort) shouldBe false
      }

      "a local number is one digit over the maximum (26 chars: '0' + 25 digits)" in {
        val localNumberTooLong = "01234567890123456789012345"
        PhoneNumber.isValid(localNumberTooLong) shouldBe false
      }

      "an international '+' number is one digit short of the minimum (7 chars: '+' + 6 digits)" in {
        val intNumberWithPlusTooShort = "+" + "12345"
        PhoneNumber.isValid(intNumberWithPlusTooShort) shouldBe false
      }

      "an international '+' number is one digit over the maximum (25 chars: '+' + 24 digits)" in {
        val intNumberWithPlusTooLong = "+" + "1234567890123456789012345"
        PhoneNumber.isValid(intNumberWithPlusTooLong) shouldBe false
      }

      "an international '00' number is one digit short of the minimum (7 chars: '00' + 6 digits)" in {
        val intNumberWithDoubleZeroTooShort = "00" + "12345"
        PhoneNumber.isValid(intNumberWithDoubleZeroTooShort) shouldBe false
      }

      "an international '00' number is one digit over the maximum (26 chars: '00' + 24 digits)" in {
        val intNumberWithDoubleZeroTooLong = "001234567890123456789012345"
        PhoneNumber.isValid(intNumberWithDoubleZeroTooLong) shouldBe false
      }

      "an international '+' number starts with '+00'" in {
        PhoneNumber.isValid("+00" + numberWithoutPrefixMinDigits) shouldBe false
      }

      "an international '+' number starts with '+440'" in {
        PhoneNumber.isValid("+440" + numberWithoutPrefixMinDigits) shouldBe false
      }

      "a '00440...' number too short for the local branch is rejected by all three branches" in {
        PhoneNumber.isValid("004401") shouldBe false
      }

      "a number is only spaces" in {
        PhoneNumber.isValid("   ") shouldBe false
      }

      "an empty string is provided" in {
        PhoneNumber.isValid("") shouldBe false
      }

      "the input has no recognised prefix (does not start with '0', '+', or '00')" in {
        PhoneNumber.isValid("1234567890") shouldBe false
      }

      "a number with spaces still fails after stripping due to wrong prefix" in {
        PhoneNumber.isValid("123 456 7890") shouldBe false
      }

      "the input contains letters mixed with digits" in {
        PhoneNumber.isValid("0161abc1234") shouldBe false
      }

      "the input contains only letters" in {
        PhoneNumber.isValid("abcdefghij") shouldBe false
      }

      "the input contains a special character other than a leading '+'" in {
        PhoneNumber.isValid("0161#111111") shouldBe false
      }
    }
  }
}