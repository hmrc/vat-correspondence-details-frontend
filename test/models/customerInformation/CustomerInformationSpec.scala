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

package models.customerInformation

import assets.CustomerInfoConstants._
import uk.gov.hmrc.play.test.UnitSpec

class CustomerInformationSpec extends UnitSpec {

  val modelNoPending: CustomerInformation = fullCustomerInfoModel.copy(pendingChanges = None)

  "CustomerInformation" should {

    "parse from JSON" when {

      "all fields are present" in {
        val result = fullCustomerInfoJson.as[CustomerInformation]
        result shouldBe fullCustomerInfoModel
      }

      "the minimum number of fields are present" in {
        val result = minCustomerInfoJson.as[CustomerInformation]
        result shouldBe minCustomerInfoModel
      }
    }
  }

  "The sameAddress val" should {

    "return true when approved and pending business address are the same" in {
      fullCustomerInfoModel.sameAddress shouldBe true
    }

    "return false when approved and pending business address are different" in {
      modelNoPending.sameAddress shouldBe false
    }
  }

  "The sameEmail val" should {

    "return true when approved and pending email address are the same" in {
      fullCustomerInfoModel.sameEmail shouldBe true
    }

    "return false when approved and pending email address are different" in {
      modelNoPending.sameEmail shouldBe false
    }
  }

  "The sameLandline val" should {

    "return true when approved and pending landline numbers are the same" in {
      fullCustomerInfoModel.sameLandline shouldBe true
    }

    "return false when approved and pending landline numbers are different" in {
      modelNoPending.sameLandline shouldBe false
    }
  }

  "The sameMobile val" should {

    "return true when approved and pending mobile numbers are the same" in {
      fullCustomerInfoModel.sameMobile shouldBe true
    }

    "return false when approved and pending mobile numbers are different" in {
      modelNoPending.sameMobile shouldBe false
    }
  }

  "The sameWebsite val" should {

    "return true when approved and pending website address are the same" in {
      fullCustomerInfoModel.sameWebsite shouldBe true
    }

    "return false when approved and pending website address are different" in {
      modelNoPending.sameWebsite shouldBe false
    }
  }

  "The pendingPpobChanges val" should {

    "return true when there are pending ppob changes" in {
      fullCustomerInfoModel.pendingPpobChanges shouldBe true
    }

    "return false when there are pending ppob changes" in {
      modelNoPending.pendingPpobChanges shouldBe false
    }
  }

  "Calling .entityName" when {

    "the model contains a trading name" should {

      "return the trading name" in {
        val result: Option[String] = fullCustomerInfoModel.entityName
        result shouldBe Some("PepsiMac")
      }
    }

    "the model does not contain a trading name or organisation name" should {

      "return the first and last name" in {
        val customerInfoSpecific = fullCustomerInfoModel.copy(tradingName = None, organisationName = None)
        val result: Option[String] = customerInfoSpecific.entityName
        result shouldBe Some("Pepsi Mac")
      }
    }

    "the model does not contain a trading name, first name or last name" should {

      "return the organisation name" in {
        val customerInfoSpecific = fullCustomerInfoModel.copy(tradingName = None, firstName = None, lastName = None)
        val result: Option[String] = customerInfoSpecific.entityName
        result shouldBe Some("PepsiMac Ltd")
      }
    }

    "the model does not contains a trading name, organisation name, or individual names" should {

      "return None" in {
        val result: Option[String] = minCustomerInfoModel.entityName
        result shouldBe None
      }
    }
  }
}
