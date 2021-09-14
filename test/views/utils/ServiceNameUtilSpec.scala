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

package views.utils

import _root_.utils.TestUtil
import org.scalatest.matchers.should.Matchers

class ServiceNameUtilSpec extends TestUtil with Matchers {

  "ServiceNameUtil.generateHeader" when {

    "given a User who is an Agent" should {

      "return the agent service name Update your client’s VAT details" in {
        ServiceNameUtil.generateHeader(agent, messages) shouldBe "Your client’s VAT details"
      }
    }

    "given a User who is not an Agent" should {

      "return the client service name Business tax account" in {
        ServiceNameUtil.generateHeader(user, messages) shouldBe "Business tax account"
      }
    }

    "not given a User" should {

      "return the client service name Business tax account" in {
        ServiceNameUtil.generateHeader(request, messages) shouldBe "VAT"
      }
    }
  }

  ".generateServiceUrl" when {

    "given a User who is an Agent" should {

      "return the Agent Hub URL" in {
        ServiceNameUtil.generateServiceUrl(agent, mockConfig) shouldBe Some(mockConfig.vatAgentClientLookupAgentHubPath)
      }
    }

    "given a User who is not an Agent" should {

      "return the BTA home URL" in {
        ServiceNameUtil.generateServiceUrl(user, mockConfig) shouldBe Some(mockConfig.btaHomeUrl)
      }
    }

    "not given a User" should {

      "return None" in {
        ServiceNameUtil.generateServiceUrl(request, mockConfig) shouldBe None
      }
    }
  }
}
