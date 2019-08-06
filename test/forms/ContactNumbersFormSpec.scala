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
import utils.TestUtil

class ContactNumbersFormSpec extends TestUtil {

  "The contact numbers form" should {

    val testLandline = "01952123456"
    val testMobile = "07890123456"

    "be able to be constructed given a landline and mobile" in {
      val actual = contactNumbersForm("", "").bind(Map("landlineNumber" -> testLandline, "mobileNumber" -> testMobile))
      actual.value shouldBe Some(ContactNumbers(Some(testLandline), Some(testMobile)))
    }
  }
}
