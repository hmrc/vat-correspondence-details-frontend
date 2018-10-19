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

package connectors

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import connectors.httpParsers.GetCustomerInfoHttpParser.{GetCustomerInfoError, GetCustomerInfoResponse}
import helpers.IntegrationBaseSpec
import models.customerInformation.{CustomerInformation, PPOB, PPOBAddress}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import stubs.VatSubscriptionStub
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

class VatSubscriptionConnectorISpec extends IntegrationBaseSpec {

  private trait Test {
    def setupStubs(): StubMapping
    val connector: VatSubscriptionConnector = app.injector.instanceOf[VatSubscriptionConnector]
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val ec: ExecutionContext = ExecutionContext.global
  }

  "Calling getCustomerInfo" when {

    "valid JSON is returned by the endpoint" should {

      "return a CustomerInformation model" in new Test {
        override def setupStubs(): StubMapping = VatSubscriptionStub.stubCustomerInfo
        setupStubs()
        val expected = Right(CustomerInformation(PPOB(
          PPOBAddress(
            "firstLine",
            None,
            None,
            None,
            None,
            None,
            "codeOfMyCountry"
          ),
          None,
          Some("www.pepsi-mac.biz")
        )))
        val result: GetCustomerInfoResponse = await(connector.getCustomerInfo("123456789"))

        result shouldBe expected
      }

      "return an Error model" in new Test {
        override def setupStubs(): StubMapping = VatSubscriptionStub.stubErrorFromApi
        setupStubs()
        val expected = Left(GetCustomerInfoError(INTERNAL_SERVER_ERROR, """{"fail":"nope"}"""))
        val result: GetCustomerInfoResponse = await(connector.getCustomerInfo("123456789"))

        result shouldBe expected
      }
    }
  }
}
