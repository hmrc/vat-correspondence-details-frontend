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

package controllers

import assets.BaseTestConstants._
import assets.CustomerInfoConstants.{customerInfoEmailUnverified, fullCustomerInfoModel, minCustomerInfoModel}
import common.SessionKeys
import common.SessionKeys._
import mocks.MockEmailVerificationService
import models.User
import models.contactPreferences.ContactPreference.digital
import models.errors.ErrorModel
import models.viewModels.ChangeSuccessViewModel
import play.api.http.Status
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers._
import views.html.templates.ChangeSuccessView

import scala.concurrent.Future

class ChangeSuccessControllerSpec extends ControllerBaseSpec with MockEmailVerificationService {

  val controller: ChangeSuccessController = new ChangeSuccessController(
    mockVatSubscriptionService,
    inject[ChangeSuccessView]
  )

  "Calling the landlineNumber action" when {

    "the landlineChangeSuccessful session key is set to true" when {

      "the user is a principal entity" should {

        lazy val result: Future[Result] = controller.landlineNumber(getRequest.withSession(landlineChangeSuccessful -> "true"))

        "return 200" in {
          mockGetCustomerInfo(vrn)(Right(fullCustomerInfoModel))
          mockGetEmailVerificationState("pepsimac@gmail.com")(Future(Some(true)))
          status(result) shouldBe Status.OK
        }
      }

      "the user is an agent" should {

        lazy val result: Future[Result] =
          controller.landlineNumber(getRequest.withSession(landlineChangeSuccessful -> "true", mtdVatvcClientVrn -> vrn))

        "return 200" in {
          mockAgentAuthorised
          mockGetCustomerInfo(vrn)(Right(fullCustomerInfoModel))
          status(result) shouldBe Status.OK
        }
      }
    }

    "the landlineChangeSuccessful session key has an unexpected value" should {

      lazy val result: Future[Result] = controller.landlineNumber(getRequest.withSession(landlineChangeSuccessful -> "false"))

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to the capture landline controller" in {
        redirectLocation(result) shouldBe Some(controllers.landlineNumber.routes.CaptureLandlineNumberController.show.url)
      }
    }

    "there is no landlineChangeSuccessful key in session" should {

      lazy val result: Future[Result] = controller.landlineNumber(getRequest)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to the capture landline controller" in {
        redirectLocation(result) shouldBe Some(controllers.landlineNumber.routes.CaptureLandlineNumberController.show.url)
      }
    }

    insolvencyCheck(controller.landlineNumber)
  }

  "Calling the mobileNumber action" when {

    "the mobileChangeSuccessful session key is set to true" when {

      "the user is a principal entity" should {

        lazy val result: Future[Result] = controller.mobileNumber(getRequest.withSession(mobileChangeSuccessful -> "true"))

        "return 200" in {
          mockGetCustomerInfo(vrn)(Right(fullCustomerInfoModel))
          mockGetEmailVerificationState("pepsimac@gmail.com")(Future(Some(true)))
          status(result) shouldBe Status.OK
        }
      }

      "the user is an agent" should {

        lazy val result: Future[Result] =
          controller.mobileNumber(getRequest.withSession(mobileChangeSuccessful -> "true", mtdVatvcClientVrn -> vrn))

        "return 200" in {
          mockAgentAuthorised
          mockGetCustomerInfo(vrn)(Right(fullCustomerInfoModel))
          status(result) shouldBe Status.OK
        }
      }
    }

    "the mobileChangeSuccessful session key has an unexpected value" should {

      lazy val result: Future[Result] = controller.mobileNumber(getRequest.withSession(mobileChangeSuccessful -> "false"))

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to the capture landline controller" in {
        redirectLocation(result) shouldBe Some(controllers.mobileNumber.routes.CaptureMobileNumberController.show.url)
      }
    }

    "there is no mobileChangeSuccessful key in session" should {

      lazy val result: Future[Result] = controller.mobileNumber(getRequest)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to the capture landline controller" in {
        redirectLocation(result) shouldBe Some(controllers.mobileNumber.routes.CaptureMobileNumberController.show.url)
      }
    }

    insolvencyCheck(controller.landlineNumber)
  }

  "Calling the websiteAddress action" when {

    "the websiteChangeSuccessful session key is set to true" when {

      "the user is a principal entity" when {

        lazy val result: Future[Result] = controller.websiteAddress(getRequest.withSession(websiteChangeSuccessful -> "true"))

        "return 200" in {
          mockGetCustomerInfo(vrn)(Right(fullCustomerInfoModel))
          mockGetEmailVerificationState("pepsimac@gmail.com")(Future(Some(true)))
          status(result) shouldBe Status.OK
        }
      }

      "the user is an agent" should {

        lazy val result: Future[Result] = {
          mockAgentAuthorised
          mockGetCustomerInfo(vrn)(Right(fullCustomerInfoModel))
          controller.websiteAddress(getRequest.withSession(websiteChangeSuccessful -> "true", mtdVatvcClientVrn -> vrn))
        }

        "return 200" in {
          status(result) shouldBe Status.OK
        }
      }
    }

    "the websiteChangeSuccessful session key has an unexpected value" should {

      lazy val result: Future[Result] = controller.websiteAddress(getRequest.withSession(websiteChangeSuccessful -> "false"))

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to the capture website address controller" in {
        redirectLocation(result) shouldBe Some(controllers.website.routes.CaptureWebsiteController.show.url)
      }
    }

    "there is no websiteChangeSuccessful key in session" should {

      lazy val result: Future[Result] = controller.websiteAddress(getRequest)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to the capture website address controller" in {
        redirectLocation(result) shouldBe Some(controllers.website.routes.CaptureWebsiteController.show.url)
      }
    }

    insolvencyCheck(controller.websiteAddress)
  }

  "The .getTitleMessageKey function should return the appropriate message key" when {

    "the landline has been changed" in {
      val result = controller.getTitleMessageKey(landlineChangeSuccessful)
      result shouldBe "landlineChangeSuccess.title.change"
    }

    "the website has been changed" in {
      val result = controller.getTitleMessageKey(websiteChangeSuccessful)
      result shouldBe "websiteChangeSuccess.title.change"
    }
  }

  "The .getClientEntityName function" when {

    "there is an entity name in session" should {

      lazy val result = controller.getClientEntityName(User(vrn, arn = Some(arn))(getRequest.withSession(
        mtdVatvcAgentClientName -> "Jorip Biscuit Co"
      )))

      "return the entity name" in {
        await(result) shouldBe Some("Jorip Biscuit Co")
      }
    }

    "there is not an entity name in session" when {

      "the call to VatSubscription is successful" should {

        "return the entity name" in {
          val result = {
            mockGetCustomerInfo(vrn)(Right(fullCustomerInfoModel))
            controller.getClientEntityName(User(vrn, arn = Some(arn))(getRequest))
          }

          await(result) shouldBe Some("PepsiMac")
        }
      }

      "the call to VatSubscription is unsuccessful" should {

        "return None" in {
          val result = {
            mockGetCustomerInfo(vrn)(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "Error")))
            controller.getClientEntityName(User(vrn, arn = Some(arn))(getRequest))
          }

          await(result) shouldBe None
        }
      }
    }
  }

  "Calling the .constructViewModel" when {

    "there is a full CustomerInformation model passed in" when {

      "there is an agent email" should {

        lazy val result = controller.constructViewModel(Some("agent@email.com"), "vatCorrespondenceLandlineChangeSuccessful", Right(fullCustomerInfoModel))
        lazy val expectedResult = ChangeSuccessViewModel(
          controller.getTitleMessageKey("vatCorrespondenceLandlineChangeSuccessful"),
          Some("agent@email.com"),
          Some(digital),
          Some("PepsiMac"),
          Some(true)
        )

        "construct the correct view model" in {

          result shouldBe expectedResult
        }
      }

      "there is no agent email" should {

        lazy val result = controller.constructViewModel(None, SessionKeys.landlineChangeSuccessful, Right(fullCustomerInfoModel))
        lazy val expectedResult = ChangeSuccessViewModel(
          controller.getTitleMessageKey(SessionKeys.landlineChangeSuccessful),
          None,
          Some(digital),
          Some("PepsiMac"),
          Some(true)
        )

        "construct the correct view model" in {

          result shouldBe expectedResult
        }
      }
    }

    "there is a minimal customer information model passed in" when {

      "there is an agent email" should {

        lazy val result = controller.constructViewModel(Some("agent@email.com"), SessionKeys.landlineChangeSuccessful, Right(minCustomerInfoModel))
        lazy val expectedResult = ChangeSuccessViewModel(
          controller.getTitleMessageKey(SessionKeys.landlineChangeSuccessful),
          Some("agent@email.com"),
          None,
          None,
          None
        )

        "construct the correct view model" in {

          result shouldBe expectedResult
        }
      }

      "there is no agent email" should {

        lazy val result = controller.constructViewModel(None, SessionKeys.landlineChangeSuccessful, Right(minCustomerInfoModel))
        lazy val expectedResult = ChangeSuccessViewModel(
          controller.getTitleMessageKey(SessionKeys.landlineChangeSuccessful),
          None,
          None,
          None,
          None
        )

        "construct the correct view model" in {

          result shouldBe expectedResult
        }
      }
    }

    "the user has an unverified email" when {

      "there is an agent email" should {

        lazy val result = controller.constructViewModel(Some("agent@email.com"),
          SessionKeys.landlineChangeSuccessful, Right(customerInfoEmailUnverified))
        lazy val expectedResult = ChangeSuccessViewModel(
          controller.getTitleMessageKey(SessionKeys.landlineChangeSuccessful),
          Some("agent@email.com"),
          Some(digital),
          Some("PepsiMac"),
          Some(false)
        )

        "construct the correct view model" in {

          result shouldBe expectedResult
        }
      }

      "there is no agent email" should {

        lazy val result = controller.constructViewModel(None, SessionKeys.landlineChangeSuccessful, Right(customerInfoEmailUnverified))
        lazy val expectedResult = ChangeSuccessViewModel(
          controller.getTitleMessageKey(SessionKeys.landlineChangeSuccessful),
          None,
          Some(digital),
          Some("PepsiMac"),
          Some(false)
        )

        "construct the correct view model" in {

          result shouldBe expectedResult
        }
      }
    }

    "there is no customer information model passed in" when {

      "there is an agent email" should {

        lazy val result = controller.constructViewModel(Some("agent@email.com"),
          SessionKeys.landlineChangeSuccessful, Left(ErrorModel(INTERNAL_SERVER_ERROR, "")))
        lazy val expectedResult = ChangeSuccessViewModel(
          controller.getTitleMessageKey(SessionKeys.landlineChangeSuccessful),
          Some("agent@email.com"),
          None,
          None,
          None
        )

        "construct the correct view model" in {

          result shouldBe expectedResult
        }
      }

      "there is no agent email" should {

        lazy val result = controller.constructViewModel(None, SessionKeys.landlineChangeSuccessful, Left(ErrorModel(INTERNAL_SERVER_ERROR, "")))
        lazy val expectedResult = ChangeSuccessViewModel(
          controller.getTitleMessageKey(SessionKeys.landlineChangeSuccessful),
          None,
          None,
          None,
          None
        )

        "construct the correct view model" in {

          result shouldBe expectedResult
        }
      }
    }
  }

  "The .sessionGuard function" when {

    "an unrecognised change key & value are provided" should {

      implicit lazy val user: User[AnyContentAsEmpty.type] =
        User[AnyContentAsEmpty.type](vrn, active = true)(getRequest.withSession("faxChangeSuccessful" -> "yep"))
      lazy val result = controller.sessionGuard("faxChangeSuccessful")

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the manage VAT change of circs overview page" in {
        redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
      }
    }
  }
}
