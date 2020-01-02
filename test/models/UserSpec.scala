/*
 * Copyright 2020 HM Revenue & Customs
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

import uk.gov.hmrc.auth.core.{AuthorisationException, Enrolment, EnrolmentIdentifier, Enrolments}
import utils.TestUtil

class UserSpec extends TestUtil {

  "Creating a User model with a VRN and an active enrolment" should {

    val enrolments = Enrolments(Set(
      Enrolment("HMRC-MTD-VAT", Seq(EnrolmentIdentifier("VRN", "123456789")), "")
    ))
    val user = User(enrolments)(request)

    "construct a user correctly with a VRN" in {
      user.vrn shouldBe "123456789"
    }

    "construct a user correctly with an active enrolment" in {
      user.active shouldBe true
    }
  }

  "Creating a User with an invalid enrolment" should {

    val enrolments = Enrolments(Set(
      Enrolment("INCORRECT-ENROLMENT", Seq(EnrolmentIdentifier("VRN", "123456789")), "")
    ))

    "throw an error with the correct error message" in {
      intercept[AuthorisationException](User(enrolments)(request)).reason shouldBe "VRN Missing"
    }
  }

}
