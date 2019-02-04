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

package models.customerInformation

import assets.CustomerInfoConstants._
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class CustomerInformationSpec extends UnitSpec {

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

    "parse to JSON" when {

      "all fields are present" in {
        val result = Json.toJson(fullCustomerInfoModel)
        result shouldBe fullCustomerInfoJson
      }

      "the minimum number of fields are present" in {
        val result = Json.toJson(minCustomerInfoModel)
        result shouldBe minCustomerInfoJson
      }
    }

    "approvedAndPendingEmailAddressMatch" when {

      "pending email and approved email are not present" should {

        val model = CustomerInformation(
          PPOB(
            minPPOBAddressModel,
            Some(ContactDetails(None, None, None, None, None)),
            None
          ),
          Some(PendingChanges(
            Some(PPOB(
              minPPOBAddressModel,
              Some(ContactDetails(None, None, None, None, None)),
              None
            ))
          ))
        )

        "return true" in {
          model.approvedAndPendingEmailAddressMatch shouldBe true
        }
      }

      "pending email does not match approved email" should {

        val model = CustomerInformation(
          PPOB(
            minPPOBAddressModel,
            Some(ContactDetails(None, None, None, Some("email"), None)),
            None
          ),
          Some(PendingChanges(
            Some(PPOB(
              minPPOBAddressModel,
              Some(ContactDetails(None, None, None, Some("different email"), None)),
              None
            ))
          ))
        )

        "return false" in {
          model.approvedAndPendingEmailAddressMatch shouldBe false
        }
      }

      "pending email matches approved email" should {

        val model = CustomerInformation(
          PPOB(
            minPPOBAddressModel,
            Some(ContactDetails(None, None, None, Some("email"), None)),
            None
          ),
          Some(PendingChanges(
            Some(PPOB(
              minPPOBAddressModel,
              Some(ContactDetails(None, None, None, Some("email"), None)),
              None
            ))
          ))
        )

        "return true" in {
          model.approvedAndPendingEmailAddressMatch shouldBe true
        }
      }
    }

    "approvedAndPendingPPOBAddressMatch" when {

      "pending PPOB does not match approved PPOB" should {

        val model = CustomerInformation(
          PPOB(
            PPOBAddress("Add", None, None, None, None, None, ""),
            None,
            None
          ),
          Some(PendingChanges(
            Some(PPOB(
              PPOBAddress("Address", None, None, None, None, None, ""),
              None,
              None
            ))
          ))
        )

        "return false" in {
          model.approvedAndPendingPPOBAddressMatch shouldBe false
        }
      }

      "pending PPOB matches approved PPOB" should {

        val model = CustomerInformation(
          PPOB(
            minPPOBAddressModel,
            None,
            None
          ),
          Some(PendingChanges(
            Some(PPOB(
              minPPOBAddressModel,
              None,
              None
            ))
          ))
        )

        "return true" in {
          model.approvedAndPendingPPOBAddressMatch shouldBe true
        }
      }
    }
  }
}
