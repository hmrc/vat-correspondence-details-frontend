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

    val invalidEmailFormatErrorMessage: String = "Enter a valid email address"
    val maxLengthErrorMessage: String = "Email address must be 132 characters or less"
    val emptyEmailErrorMessage: String = "Enter your email address"
    val notChangedErrorMessage: String = "Enter a different email address"

    val testEmailLocalPart: String = "user"
    val testEmailDomain: String = "@example.com"
    val testEmail: String = s"$testEmailLocalPart$testEmailDomain"

    "validate that testEmail is valid" in {
      val actual = emailForm("").bind(Map("email" -> testEmail)).value
      actual shouldBe Some(testEmail)
    }

    "validate our controlled email where the domain is a valid IP format" in {
      val expectedEmail = testEmailLocalPart + "@111.222.333.444"
      val actual = emailForm("").bind(Map("email" -> expectedEmail)).value
      actual shouldBe Some(expectedEmail)
    }

    "validate our controlled email where the local-part contain only numbers" in {
      val expectedEmail = "1234567890" + testEmailDomain
      val actual = emailForm("").bind(Map("email" -> expectedEmail)).value
      actual shouldBe Some(expectedEmail)
    }

    "validate our controlled email where the local-part contains legal special characters" in {
      val expectedEmail = "#!$%&'*+-/=?^_`{}|~" + testEmailDomain
      val actual = emailForm("").bind(Map("email" -> expectedEmail)).value
      actual shouldBe Some(expectedEmail)
    }

    "validate our controlled email when there are separations in the domain name" in {
      val expectedEmail = testEmailLocalPart + "@a.a-a.com"
      val actual = emailForm("").bind(Map("email" -> expectedEmail)).value
      actual shouldBe Some(expectedEmail)
    }

    "validate that data has been entered" in {
      val formWithError = emailForm("").bind(Map("email" -> ""))
      formWithError.errors should contain(FormError("email", emptyEmailErrorMessage))
    }

    "validate that the current email is different (case-insensitive)" in {
      val formWithError = emailForm(testEmail.toUpperCase).bind(Map("email" -> testEmail.toLowerCase))
      formWithError.errors should contain(FormError("email", notChangedErrorMessage))
    }

    "validate that invalid email fails" in {
      val formWithError = emailForm("").bind(Map("email" -> "invalid"))
      formWithError.errors should contain(FormError("email", invalidEmailFormatErrorMessage))
    }

    "validate that invalid email fails where the domain contains 2 dots" in {
      val testEmail = testEmailLocalPart + "@a..b"
      val formWithError = emailForm("").bind(Map("email" -> testEmail))
      formWithError.errors should contain(FormError("email", invalidEmailFormatErrorMessage))
    }

    "validate that invalid email fails where domain does not contain dots" in {
      val testEmail = testEmailLocalPart + "@a"
      val formWithError = emailForm("").bind(Map("email" -> testEmail))
      formWithError.errors should contain(FormError("email", invalidEmailFormatErrorMessage))
    }

    "validate that invalid email fails where the domain contains multiple @ symbols" in {
      val testEmail = testEmailLocalPart + "a@a@"
      val formWithError = emailForm("").bind(Map("email" -> testEmail))
      formWithError.errors should contain(FormError("email", invalidEmailFormatErrorMessage))
    }

    "validate that invalid email fails where local-part contains illegal characters without quotes" in {
      val testEmail = "this is\"not\\allowed" + testEmailDomain
      val formWithError = emailForm("").bind(Map("email" -> testEmail))
      formWithError.errors should contain(FormError("email", invalidEmailFormatErrorMessage))
    }

    "validate that invalid email fails where unicode chars included in local-part" in {
      val testEmail = "あいうえお" + testEmailDomain
      val formWithError = emailForm("").bind(Map("email" -> testEmail))
      formWithError.errors should contain(FormError("email", invalidEmailFormatErrorMessage))
    }

    "validate that invalid email fails where local-part email not included" in {
      val testEmail = testEmailDomain
      val formWithError = emailForm("").bind(Map("email" -> testEmail))
      formWithError.errors should contain(FormError("email", invalidEmailFormatErrorMessage))
    }

    "validate that invalid email fails where encoded html included" in {
      val testEmail = "Joe Smith <" + testEmailLocalPart + testEmailDomain + ">"
      val formWithError = emailForm("").bind(Map("email" -> testEmail))
      formWithError.errors should contain(FormError("email", invalidEmailFormatErrorMessage))
    }

    "validate that email does not exceed max length" in {
      val exceed = emailForm("").bind(Map("email" -> ("a" * (maxLength + 1)))).errors
      exceed should contain(FormError("email", maxLengthErrorMessage))
      exceed.seq.size shouldBe 1
    }

    "validate that email allows max length" in {
      val errors = emailForm("").bind(Map("email" -> ("a" * maxLength))).errors
      errors should not contain FormError("email", maxLengthErrorMessage)
    }
  }
}
