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

package services

import assets.BaseTestConstants._
import assets.CustomerInfoConstants.{fullCustomerInfoModel, _}
import mocks.{MockEmailVerificationService, MockVatSubscriptionConnector}
import models.User
import models.contactPreferences.ContactPreference
import models.customerInformation._
import models.errors.ErrorModel
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import utils.TestUtil
import scala.concurrent.Future

class VatSubscriptionServiceSpec extends TestUtil with MockVatSubscriptionConnector with
  MockEmailVerificationService {

  val service = new VatSubscriptionService(connector, mockEmailVerificationService)
  val testVrn: String = "123456789"
  implicit val testUser: User[AnyContentAsEmpty.type] = user

  override def beforeEach(): Unit = {
    super.beforeEach()
    mockConfig.features.emailVerifiedContactPrefEnabled(true)
  }

  "calling getCustomerInfo" when {

    "the VatSubscriptionConnector returns a model" should {

      "return the model" in {
        mockGetCustomerInfoSuccessResponse()

        val result = await(service.getCustomerInfo(testVrn))
        result shouldBe Right(fullCustomerInfoModel)
      }
    }

    "the VatSubscriptionConnector returns an error" should {

      "return the error" in {
        mockGetCustomerInfoFailureResponse()

        val result = await(service.getCustomerInfo(testVrn))
        result shouldBe Left(invalidJsonError)
      }
    }
  }

  "calling updateEmail" when {

    "the email has been verified and the email update has been successful" should {

      "return the model" in {
        mockGetEmailVerificationState(testEmail)(Future(Some(true)))
        mockGetCustomerInfoSuccessResponse()
        mockUpdatePPOBSuccessResponse()

        val result = await(service.updateEmail(testVrn, testEmail))
        result shouldBe Right(UpdatePPOBSuccess("success"))
      }
    }

    "the VatSubscriptionConnector returns an error" should {

      "return the error" in {
        mockGetEmailVerificationState(testEmail)(Future(Some(true)))
        mockGetCustomerInfoSuccessResponse()
        mockUpdatePPOBFailureResponse()

        val result = await(service.updateEmail(testVrn, testEmail))
        result shouldBe Left(invalidJsonError)
      }
    }

    "the email has not been verified" should {

      "return the error" in {
        mockGetEmailVerificationState(testEmail)(Future(Some(false)))

        val result = await(service.updateEmail(testVrn, testEmail))
        result shouldBe Right(UpdatePPOBSuccess(""))
      }
    }

    "there was an unexpected response from email verification service" should {

      "return the error" in {
        mockGetEmailVerificationState(testEmail)(Future(None))

        val result = await(service.updateEmail(testVrn, testEmail))
        result shouldBe Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "Couldn't verify email address"))
      }
    }
  }

  "calling updateWebsite" when {

    "the update is successful" should {

      "return the success model" in {
        mockGetCustomerInfoSuccessResponse()
        mockUpdatePPOBSuccessResponse()
        val result = await(service.updateWebsite(testVrn, testWebsite))
        result shouldBe Right(UpdatePPOBSuccess("success"))
      }
    }

    "the VatSubscriptionConnector returns an error for the getCustomerInfo call" should {

      "return the error" in {
        mockGetCustomerInfoFailureResponse()
        val result = await(service.updateWebsite(testVrn, testWebsite))
        result shouldBe Left(invalidJsonError)
      }
    }

    "the VatSubscriptionConnector returns an error for the updatePPOB call" should {

      "return the error" in {
        mockGetCustomerInfoSuccessResponse()
        mockUpdatePPOBFailureResponse()
        val result = await(service.updateWebsite(testVrn, testWebsite))
        result shouldBe Left(invalidJsonError)
      }
    }
  }

  "calling buildEmailUpdateModel" when {

    "the user has existing contact details" should {

      "return a PPOB model with the updated email" in {
        val expectedPPOB: PPOB = PPOB(
          fullPPOBAddressModel,
          Some(ContactDetails(
            Some("01234567890"),
            Some("07707707707"),
            Some("0123456789"),
            Some(testEmail),
            Some(true)
          )),
          Some("www.pepsi-mac.biz")
        )

        val result = service.buildEmailUpdateModel(testEmail, fullPPOBModel)
        result shouldBe expectedPPOB
      }
    }

    "the user does not have contact details" should {

      "return a PPOB model with the new email" in {
        val expectedPPOB: PPOB = PPOB(
          minPPOBAddressModel,
          Some(ContactDetails(
            None,
            None,
            None,
            Some(testEmail),
            Some(true)
          )),
          None
        )

        val result = service.buildEmailUpdateModel(testEmail, minPPOBModel)
        result shouldBe expectedPPOB
      }
    }
  }

  "calling buildWebsiteUpdateModel" when {

    "the website address is not empty" should {

      "return a PPOB model with the updated website address" in {

        val expectedPPOB: PPOB = PPOB(
          minPPOBAddressModel,
          None,
          Some(testWebsite)
        )
        val result = service.buildWebsiteUpdateModel(testWebsite, minPPOBModel)
        result shouldBe expectedPPOB
      }
    }
  }

  "calling buildLandlineUpdateModel" when {

    "the user has existing contact details" should {

      "return a PPOB model with the updated landline number" in {
        val expectedPPOB: PPOB = PPOB(
          fullPPOBAddressModel,
          Some(ContactDetails(
            Some(testPrepopLandline),
            Some("07707707707"),
            Some("0123456789"),
            Some("pepsimac@gmail.com"),
            Some(true)
          )),
          Some("www.pepsi-mac.biz")
        )

        val result = service.buildLandlineUpdateModel(testPrepopLandline, fullPPOBModel)
        result shouldBe expectedPPOB
      }
    }

    "the user does not have contact details" should {

      "return a PPOB model with the update landline number" in {
        val expectedPPOB: PPOB = PPOB(
          minPPOBAddressModel,
          Some(ContactDetails(
            Some(testPrepopLandline),
            None,
            None,
            None,
            None
          )),
          None
        )

        val result = service.buildLandlineUpdateModel(testPrepopLandline, minPPOBModel)
        result shouldBe expectedPPOB
      }
    }
  }

  "calling buildMobileUpdateModel" when {

    "the user has existing contact details" should {

      "return a PPOB model with the updated mobile number" in {
        val expectedPPOB: PPOB = PPOB(
          fullPPOBAddressModel,
          Some(ContactDetails(
            Some("01234567890"),
            Some(testPrepopMobile),
            Some("0123456789"),
            Some("pepsimac@gmail.com"),
            Some(true)
          )),
          Some("www.pepsi-mac.biz")
        )

        val result = service.buildMobileUpdateModel(testPrepopMobile, fullPPOBModel)
        result shouldBe expectedPPOB
      }
    }

    "the user does not have contact details" should {

      "return a PPOB model with the updated mobile number" in {
        val expectedPPOB: PPOB = PPOB(
          minPPOBAddressModel,
          Some(ContactDetails(
            None,
            Some(testPrepopMobile),
            None,
            None,
            None
          )),
          None
        )

        val result = service.buildMobileUpdateModel(testPrepopMobile, minPPOBModel)
        result shouldBe expectedPPOB
      }
    }
  }

  "the website address is empty" should {

    "return a PPOB model with the website address field set to None" in {

      val expectedPPOB: PPOB = PPOB(
        fullPPOBAddressModel,
        Some(fullContactDetailsModel),
        None
      )
      val result = service.buildWebsiteUpdateModel("", fullPPOBModel)
      result shouldBe expectedPPOB
    }
  }

  "calling updateLandlineNumber" when {

    "the landline number has been verified and the update has been successful" should {

      "return the model" in {
        mockGetCustomerInfoSuccessResponse()
        mockUpdatePPOBSuccessResponse()

        val result = await(service.updateLandlineNumber(testVrn, testPrepopLandline))
        result shouldBe Right(UpdatePPOBSuccess("success"))
      }
    }

    "the VatSubscriptionConnector returns an error" should {

      "return the error" in {
        mockGetCustomerInfoSuccessResponse()
        mockUpdatePPOBFailureResponse()

        val result = await(service.updateLandlineNumber(testVrn, testPrepopLandline))
        result shouldBe Left(invalidJsonError)
      }
    }
  }

  "calling updateMobileNumber" when {

    "the mobile number has been verified and the update has been successful" should {

      "return the model" in {
        mockGetCustomerInfoSuccessResponse()
        mockUpdatePPOBSuccessResponse()

        val result = await(service.updateMobileNumber(testVrn, testPrepopMobile))
        result shouldBe Right(UpdatePPOBSuccess("success"))
      }
    }

    "the VatSubscriptionConnector returns an error" should {

      "return the error" in {
        mockGetCustomerInfoSuccessResponse()
        mockUpdatePPOBFailureResponse()

        val result = await(service.updateMobileNumber(testVrn, testPrepopMobile))
        result shouldBe Left(invalidJsonError)
      }
    }
  }

  "The .getVerifiedEmailStatus function" when {

    "the feature switch is on" should {

      "call getCustomerInfo successfully" when {

        "a user has a digital preference and a verified email" should {

          "return email verification status - emailVerified" in {
            val result = {
              mockGetCustomerInfoSuccessResponse
              service.getEmailVerifiedStatus(user.vrn, Right(ContactPreference("DIGITAL")))
            }
            await(result) shouldBe Some(true)
          }
        }

        "a user has a paper preference" should {

          "return None for emailVerified" in {

            val result = {
              service.getEmailVerifiedStatus(user.vrn, Right(ContactPreference("PAPER")))
            }
            await(result) shouldBe None
          }
        }
      }

      "not call .getCustomerInfo successfully" should {

        "return None" in {
          val result = {
            mockGetCustomerInfoFailureResponse
            service.getEmailVerifiedStatus(user.vrn, Right(ContactPreference("DIGITAL")))
          }
          await(result) shouldBe None
        }
      }
    }

    "the feature switch is off" should {

      "not call the .getCustomerInfo function" in {
        val result = {
          mockConfig.features.emailVerifiedContactPrefEnabled(false)
          service.getEmailVerifiedStatus(user.vrn, Right(ContactPreference("DIGITAL")))
        }
        await(result) shouldBe None
      }
    }
  }
}