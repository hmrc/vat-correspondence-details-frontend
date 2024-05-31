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

import forms.WebsiteForm._
import org.scalatest.matchers.should.Matchers
import play.api.data.FormError
import utils.TestUtil

class WebsiteFormSpec extends TestUtil with Matchers {

  "The website form" should {

    val invalidWebsiteFormat: String = "captureWebsite.error.invalid"
    val maxLengthErrorMessage: String = "captureWebsite.error.exceedsMaxLength"
    val noEntryErrorMessage: String = "captureWebsite.error.empty"
    val unchangedErrorMessage: String = "captureWebsite.error.notChanged"

    val testWebsite = "example-test.com"

    "validate that testWebsite is valid" in {
      val actual = websiteForm("").bind(Map("website" -> testWebsite))
      actual.value shouldBe Some(testWebsite)
    }

    "validate that testWebsite capitalised is valid" in {
      val actual = websiteForm("").bind(Map("website" -> testWebsite.toUpperCase()))
      actual.value shouldBe Some(testWebsite.toUpperCase())
    }

    "validate that testWebsite is valid with protocol provided" in {
      val actual = websiteForm("").bind(Map("website" -> ("http://" + testWebsite)))
      actual.value shouldBe Some("http://" + testWebsite)
    }

    "validate that testWebsite is valid with the www prefix provided" in {
      val actual = websiteForm("").bind(Map("website" -> ("www." + testWebsite)))
      actual.value shouldBe Some("www." + testWebsite)
    }

    "validate that testWebsite is valid with the www prefix provided and the protocol provided" in {
      val actual = websiteForm("").bind(Map("website" -> ("https://www." + testWebsite)))
      actual.value shouldBe Some("https://www." + testWebsite)
    }

    "validate that an invalid website fails" in {
      val formWithError = websiteForm("").bind(Map("website" -> "invalid"))
      formWithError.errors should contain(FormError("website", invalidWebsiteFormat))
    }

    "validate that website does not exceed max length" in {
      val exceed = websiteForm("").bind(Map("website" -> ("a" * (maxLength + 1)))).errors
      exceed should contain(FormError("website", maxLengthErrorMessage))
      exceed.size shouldBe 1
    }

    "validate that email allows max length" in {
      val errors = websiteForm("").bind(Map("website" -> ("a" * maxLength))).errors
      errors should not contain FormError("website", maxLengthErrorMessage)
    }

    "validate the website is not empty" in {
      val errors = websiteForm("").bind(Map("website" -> "")).errors
      errors should contain(FormError("website", noEntryErrorMessage))
    }

    "validate the website has been changed" in {
      val errors = websiteForm(testWebsite).bind(Map("website" -> testWebsite)).errors
      errors should contain(FormError("website", unchangedErrorMessage))
    }
  }
}
