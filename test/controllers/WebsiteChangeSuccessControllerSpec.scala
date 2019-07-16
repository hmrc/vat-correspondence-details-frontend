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

package controllers

import assets.CustomerInfoConstants.fullCustomerInfoModel
import common.SessionKeys.{prepopulationWebsiteKey, websiteChangeSuccessful, clientVrn}
import mocks.MockContactPreferenceService
import models.contactPreferences.ContactPreference
import org.mockito.Mockito.{verify, times}
import org.mockito.ArgumentMatchers.any
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.Helpers._
import views.html.WebsiteChangeSuccessView

import scala.concurrent.Future

class WebsiteChangeSuccessControllerSpec extends ControllerBaseSpec with MockContactPreferenceService {

  def controller: WebsiteChangeSuccessController = new WebsiteChangeSuccessController(
    mcc,
    mockAuthPredicateComponents,
    injector.instanceOf[WebsiteChangeSuccessView],
    mockContactPreferenceService,
    mockVatSubscriptionService
  )

  "Calling the show action" when {

    "both expected session keys are populated" when {

      "the user is a principal entity" should {

        lazy val result: Future[Result] = controller.show(
          request.withSession(prepopulationWebsiteKey -> "", websiteChangeSuccessful -> "true")
        )

        "return 200" in {
          getMockContactPreference("999999999")(Future.successful(Right(ContactPreference("DIGITAL"))))
          status(result) shouldBe Status.OK
        }

        "not call the VatSubscription service" in {
          verify(mockVatSubscriptionService, times(0)).getCustomerInfo(any())(any(), any())
        }
      }

      "the user is an agent" should {

        lazy val result: Future[Result] = controller.show(
          request.withSession(prepopulationWebsiteKey -> "", websiteChangeSuccessful -> "true", clientVrn -> "999999999")
        )

        "return 200" in {
          mockAgentAuthorised()
          mockGetCustomerInfo("999999999")(Future.successful(Right(fullCustomerInfoModel)))
          status(result) shouldBe Status.OK
        }

        "not call the ContactPreferences service" in {
          verify(mockContactPreferenceService, times(0)).getContactPreference(any())(any(), any())
        }
      }
    }

    "one or more of the expected session keys is missing" should {

      lazy val result: Future[Result] = controller.show(request)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect the user to the capture website address page" in {
        redirectLocation(result) shouldBe Some(controllers.routes.CaptureWebsiteController.show().url)
      }
    }
  }
}
