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

package config.features

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import uk.gov.hmrc.play.test.UnitSpec

class FeatureSpec extends UnitSpec with GuiceOneAppPerSuite with BeforeAndAfterEach {

  private val features = new Features(app.injector.instanceOf[Configuration])

  override def beforeEach(): Unit = {
    super.beforeEach()
    features.agentAccessEnabled(true)
    features.emailVerificationEnabled(true)
    features.changeContactDetailsEnabled(true)
  }

  "The Agent Access Feature" should {

    "return its current state" in {
      features.agentAccessEnabled() shouldBe true
    }

    "switch to a new state" in {
      features.agentAccessEnabled(false)
      features.agentAccessEnabled() shouldBe false
    }
  }

  "The Email Verification Feature" should {

    "return its current state" in {
      features.emailVerificationEnabled() shouldBe true
    }

    "switch to a new state" in {
      features.emailVerificationEnabled(false)
      features.emailVerificationEnabled() shouldBe false
    }
  }

  "The Change Website Feature" should {

    "return its current state" in {
      features.changeContactDetailsEnabled() shouldBe true
    }

    "switch to a new state" in {
      features.changeContactDetailsEnabled(false)
      features.changeContactDetailsEnabled() shouldBe false
    }
  }
}
