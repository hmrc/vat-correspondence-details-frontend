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

package services

import assets.CustomerInfoConstants._
import mocks.MockVatSubscriptionConnector
import utils.TestUtil

class VatSubscriptionServiceSpec extends TestUtil with MockVatSubscriptionConnector {

  val service = new VatSubscriptionService(connector)

  "The getCustomerInfo function" when {

    "a CustomerInformation model is returned by the connector" should {

      "return the model" in {
        mockSuccessResponse()
        val result = await(service.getCustomerInfo("123456789"))
        result shouldBe Right(customerInfoModel)
      }
    }

    "an error is returned by the connector" should {

      "return the error" in {
        mockFailureResponse()
        val result = await(service.getCustomerInfo("123456789"))
        result shouldBe Left(invalidJsonError)
      }
    }
  }
}
