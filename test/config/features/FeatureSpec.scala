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

package config.features

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration

class FeatureSpec extends AnyWordSpecLike with Matchers with GuiceOneAppPerSuite with BeforeAndAfterEach {

  private val features = new Features(app.injector.instanceOf[Configuration])

  override def beforeEach(): Unit = {
    super.beforeEach()
    features.emailVerificationEnabled(true)
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
}
