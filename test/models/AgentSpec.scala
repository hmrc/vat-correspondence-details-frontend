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

package models

import org.scalatest.matchers.should.Matchers
import utils.TestUtil
import uk.gov.hmrc.auth.core.{AuthorisationException, Enrolment, EnrolmentIdentifier, Enrolments}

class AgentSpec extends TestUtil with Matchers {

  "Creating an Agent with only an ARN" should {

    val agent = Agent("ABCD12345678901")(request)

    "construct an Agent correctly" in {
      agent.arn shouldBe "ABCD12345678901"
    }
  }

  "Creating an Agent with a valid enrolment" should {

    val enrolments = Enrolments(Set(
      Enrolment("HMRC-AS-AGENT", Seq(EnrolmentIdentifier("ARN", "ZYXW10987654321")), "")
    ))
    val agent = Agent(enrolments)(request)

    "construct an Agent correctly" in {
      agent.arn shouldBe "ZYXW10987654321"
    }
  }

  "Creating an Agent with an invalid enrolment" should {

    val enrolments = Enrolments(Set(
      Enrolment("HMRC-MTD-VAT", Seq(EnrolmentIdentifier("VRN", "123456789")), "")
    ))

    "throw an internal server error with the correct message in the exception" in {
      intercept[AuthorisationException](Agent(enrolments)(request)).reason shouldBe "Agent Service Enrolment Missing"
    }
  }
}
