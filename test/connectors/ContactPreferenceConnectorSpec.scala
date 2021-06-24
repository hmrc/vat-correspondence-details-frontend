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

package connectors

import assets.BaseTestConstants._
import connectors.httpParsers.ResponseHttpParser.HttpGetResult
import mocks.MockHttp
import models.contactPreferences.ContactPreference
import utils.TestUtil
import scala.concurrent.Future

class ContactPreferenceConnectorSpec extends TestUtil with MockHttp {

  object TestContactPreferenceConnector extends ContactPreferenceConnector(mockHttp, mockConfig)

  "ContactPreferenceConnector" when {

    def getContactPreferenceResult: Future[HttpGetResult[ContactPreference]] = TestContactPreferenceConnector.getContactPreference(vrn)

    "the service returns a success" should {

      "return a ContactPreference model" in {
        setupMockHttpGet(mockConfig.contactPreferencesUrl(vrn))(Right(ContactPreference("DIGITAL")))
        await(getContactPreferenceResult) shouldBe Right(ContactPreference("DIGITAL"))
      }
    }

    "the service returns an error" should {

      "return an ErrorModel" in {
        setupMockHttpGet(mockConfig.contactPreferencesUrl(vrn))(Left(errorModel))
        await(getContactPreferenceResult) shouldBe Left(errorModel)
      }
    }
  }
}
