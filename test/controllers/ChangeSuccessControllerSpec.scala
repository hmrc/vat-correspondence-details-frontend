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

package controllers

import assets.BaseTestConstants._
import assets.CustomerInfoConstants.fullCustomerInfoModel
import common.SessionKeys._
import mocks.{MockContactPreferenceService, MockEmailVerificationService}
import models.User
import models.contactPreferences.ContactPreference
import models.errors.ErrorModel
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.Helpers._
import views.html.templates.ChangeSuccessView

import scala.concurrent.Future

class ChangeSuccessControllerSpec extends ControllerBaseSpec with MockContactPreferenceService with MockEmailVerificationService {

  val controller: ChangeSuccessController = new ChangeSuccessController(
    mockVatSubscriptionService,
    inject[ChangeSuccessView]
  )

  "Calling the landlineNumber action" when {

    "both expected session keys are populated" when {

      "the user is a principal entity" when {

        "the contactPrefMigrationEnabled feature switch is turned on" should {
          lazy val result: Future[Result] = {
            controller.landlineNumber(request.withSession(
              landlineChangeSuccessful -> "true", prepopulationLandlineKey -> testPrepopLandline
            ))
          }

          "return 200" in {
            mockIndividualAuthorised()
            mockGetCustomerInfo(vrn)(Right(fullCustomerInfoModel))
            getMockContactPreference(vrn)(Future(Right(ContactPreference("DIGITAL"))))
            mockGetEmailVerificationState("pepsimac@gmail.com")(Future(Some(true)))
            status(result) shouldBe Status.OK
          }
        }

        "the user is an agent" should {
          lazy val result: Future[Result] = {
            controller.landlineNumber(request.withSession(
              landlineChangeSuccessful -> "true", prepopulationLandlineKey -> testPrepopLandline, clientVrn -> vrn
            ))
          }

          "return 200" in {
            mockAgentAuthorised()
            mockGetCustomerInfo(vrn)(Future.successful(Right(fullCustomerInfoModel)))
            status(result) shouldBe Status.OK
          }
        }
      }

      "one or more of the expected session keys is missing" should {

        lazy val result: Future[Result] = controller.landlineNumber(request)

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect the user to the capture landline controller" in {
          redirectLocation(result) shouldBe Some(controllers.landlineNumber.routes.CaptureLandlineNumberController.show().url)
        }
      }
    }

    insolvencyCheck(controller.landlineNumber)
  }

  "Calling the websiteAddress action" when {

    "both expected session keys are populated" when {

      "the user is a principal entity" when {

        "the contactPrefMigrationEnabled feature switch is turned on" should {

          lazy val result: Future[Result] = {
            mockGetCustomerInfo(vrn)(Right(fullCustomerInfoModel))
            controller.websiteAddress(request.withSession(
              prepopulationWebsiteKey -> "", websiteChangeSuccessful -> "true"
            ))
          }

          "return 200" in {
            status(result) shouldBe Status.OK
          }
        }

        "the user is an agent" should {

          lazy val result: Future[Result] = {
            mockAgentAuthorised()
            mockGetCustomerInfo(vrn)(Future.successful(Right(fullCustomerInfoModel)))
            controller.websiteAddress(request.withSession(
              prepopulationWebsiteKey -> "", websiteChangeSuccessful -> "true", clientVrn -> vrn
            ))
          }

          "return 200" in {
            status(result) shouldBe Status.OK
          }
        }
      }

      "one or more of the expected session keys is missing" should {

        lazy val result: Future[Result] = controller.websiteAddress(request)

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect the user to the capture website address controller" in {
          redirectLocation(result) shouldBe Some(controllers.website.routes.CaptureWebsiteController.show().url)
        }
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

      lazy val result = controller.getClientEntityName(User(vrn, arn = Some(arn))(request.withSession(
        mtdVatAgentClientName -> "Jorip Biscuit Co"
      )))

      "return the entity name" in {
        await(result) shouldBe Some("Jorip Biscuit Co")
      }
    }

    "there is not an entity name in session" when {

      "the call to VatSubscription is successful" should {

        "return the entity name" in {
          val result = {
            mockGetCustomerInfo(vrn)(Future.successful(Right(fullCustomerInfoModel)))
            controller.getClientEntityName(User(vrn, arn = Some(arn))(request))
          }

          await(result) shouldBe Some("PepsiMac")
        }
      }

      "the call to VatSubscription is unsuccessful" should {

        "return None" in {
          val result = {
            mockGetCustomerInfo(vrn)(Future.successful(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "Error"))))
            controller.getClientEntityName(User(vrn, arn = Some(arn))(request))
          }

          await(result) shouldBe None
        }
      }
    }
  }
}
