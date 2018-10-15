/*
 * Copyright 2018 HM Revenue & Customs
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

import forms.EmailForm._

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.FormError

class EmailFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  "The emailForm" should {

    val validateEmailForm = emailForm

    val invalid_email_format_error_key: String = "error.invalid_email"
    val maxlength_error_key: String = "error.exceeds_max_length_email"
    val not_entered_error_key: String = "error.empty_email"

    val testEmailLocalPart: String = "user"
    val testEmailDomain: String = "@example.com"
    val testEmail: String = s"$testEmailLocalPart$testEmailDomain"

    "validate that testEmail is valid" in {
      val actual = validateEmailForm.bind(Map(email -> testEmail)).value
      actual shouldBe Some(testEmail)
    }

    "validate our controlled email sections are valid" in {
      val controlledTestEmail = testEmailLocalPart + testEmailDomain
      val actual = validateEmailForm.bind(Map(email -> controlledTestEmail)).value
      actual shouldBe Some(controlledTestEmail)
    }

    "validate our controlled email with no separator in the local-part " in {
      val controlledTestEmail = "test" + testEmailDomain
      val actual = validateEmailForm.bind(Map(email -> controlledTestEmail)).value
      actual shouldBe Some(controlledTestEmail)
    }

    "validate our controlled email where the domain is a valid IP format" in {
      val controlledTestEmail = testEmailLocalPart + "@111.222.333.444"
      val actual = validateEmailForm.bind(Map(email -> controlledTestEmail)).value
      actual shouldBe Some(controlledTestEmail)
    }

    "validate our controlled email where the local-part contain only numbers" in {
      val controlledTestEmail = "1234567890" + testEmailDomain
      val actual = validateEmailForm.bind(Map(email -> controlledTestEmail)).value
      actual shouldBe Some(controlledTestEmail)
    }

    "validate our controlled email where the local-part contains legal special characters" in {
      val controlledTestEmail = "#!$%&'*+-/=?^_`{}|~" + testEmailDomain
      val actual = validateEmailForm.bind(Map(email -> controlledTestEmail)).value
      actual shouldBe Some(controlledTestEmail)
    }

    "validate our controlled email when there are separations in the domain name" in {
      val controlledTestEmail = testEmailLocalPart + "@a.a-a.com"
      val actual = validateEmailForm.bind(Map(email -> controlledTestEmail)).value
      actual shouldBe Some(controlledTestEmail)
    }

    "validate that data has been entered" in {
      val formWithError = validateEmailForm.bind(Map(email -> ""))
      formWithError.errors should contain(FormError(email, not_entered_error_key))
    }

    "validate that invalid email fails" in {
      val formWithError = validateEmailForm.bind(Map(email -> "invalid"))
      formWithError.errors should contain(FormError(email, invalid_email_format_error_key))
    }

    "validate that invalid email fails where the domain contains 2 dots" in {
      val testEmail = testEmailLocalPart + "@a..b"
      val formWithError = validateEmailForm.bind(Map(email -> testEmail))
      formWithError.errors should contain(FormError(email, invalid_email_format_error_key))
    }

    "validate that invalid email fails where domain does not contain dots" in {
      val testEmail = testEmailLocalPart + "@a"
      val formWithError = validateEmailForm.bind(Map(email -> testEmail))
      formWithError.errors should contain(FormError(email, invalid_email_format_error_key))
    }

    "validate that invalid email fails where the domain contains multiple @ symbols" in {
      val testEmail = testEmailLocalPart + "a@a@"
      val formWithError = validateEmailForm.bind(Map(email -> testEmail))
      formWithError.errors should contain(FormError(email, invalid_email_format_error_key))
    }

    "validate that invalid email fails where local-part contains illegal characters without quotes" in {
      val testEmail = "this is\"not\\allowed" + testEmailDomain
      val formWithError = validateEmailForm.bind(Map(email -> testEmail))
      formWithError.errors should contain(FormError(email, invalid_email_format_error_key))
    }

    "validate that invalid email fails where unicode chars included in local-part" in {
      val testEmail = "あいうえお" + testEmailDomain
      val formWithError = validateEmailForm.bind(Map(email -> testEmail))
      formWithError.errors should contain(FormError(email, invalid_email_format_error_key))
    }

    "validate that invalid email fails where local-part email not included" in {
      val testEmail = testEmailDomain
      val formWithError = validateEmailForm.bind(Map(email -> testEmail))
      formWithError.errors should contain(FormError(email, invalid_email_format_error_key))
    }

    "validate that invalid email fails where encoded html included" in {
      val testEmail = "Joe Smith <" + testEmailLocalPart + testEmailDomain + ">"
      val formWithError = validateEmailForm.bind(Map(email -> testEmail))
      formWithError.errors should contain(FormError(email, invalid_email_format_error_key))
    }

    "validate that email does not exceed max length" in {
      val exceed = validateEmailForm.bind(Map(email -> ("a" * (maxLength + 1)))).errors
      exceed should contain(FormError(email, maxlength_error_key))
      exceed.seq.size shouldBe 1
    }

    "validate that email allows max length" in {
      val errors = validateEmailForm.bind(Map(email -> ("a" * maxLength))).errors
      errors should not contain FormError(email, maxlength_error_key)
    }
  }
}
