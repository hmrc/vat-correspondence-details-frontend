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

package controllers.website

import assets.BaseTestConstants.vrn
import common.SessionKeys
import controllers.ControllerBaseSpec
import models.User
import play.api.http.Status
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.website.ConfirmRemoveWebsiteView

class ConfirmRemoveWebsiteControllerSpec extends ControllerBaseSpec  {

  lazy val controller = new ConfirmRemoveWebsiteController(
    mockErrorHandler,
    mockVatSubscriptionService,
    injector.instanceOf[ConfirmRemoveWebsiteView]
  )

  lazy val requestWithValidationWebsiteKey: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(SessionKeys.validationWebsiteKey -> testWebsite)

  "Calling the extractWebsite function in ConfirmRemoveWebsiteController" when {

    "there is an authenticated request from a user with an website address in session" should {

      "result in an website address being retrieved if there is an website" in {
        val user = User[AnyContent](vrn, active = true, None)(requestWithValidationWebsiteKey)

        controller.extractSessionWebsiteAddress(user) shouldBe Some(testWebsite)
      }
    }
  }

  "Calling the show action in ConfirmWebsiteController" when {

    "there is an website address in session" should {

      "return 200" in {
        val result = controller.show(requestWithValidationWebsiteKey)
        status(result) shouldBe Status.OK
      }
    }

    "there isn't a website address in session" should {

      lazy val result = controller.show(request)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the capture website page" in {
        redirectLocation(result) shouldBe Some(routes.CaptureWebsiteController.show().url)
      }
    }

    "the user is not authorised" should {

      "return 403" in {
        val result = {
          mockIndividualWithoutEnrolment()
          controller.show(requestWithValidationWebsiteKey)
        }

        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }

  "Calling the removeWebsiteAddress() action in ConfirmWebsiteController" when {

    "there is a validation website address in session" should {

      lazy val result = controller.removeWebsiteAddress()(requestWithValidationWebsiteKey)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the updateWebsite() action in ConfirmWebsiteController" in {
        redirectLocation(result) shouldBe Some(routes.ConfirmWebsiteController.updateWebsite().url)
      }

      "add a blank value to the prepopulation session key" in {
        session(result).get(SessionKeys.prepopulationWebsiteKey) shouldBe Some("")
      }
    }

    "there isn't a validation website address in session" should {

      lazy val result = controller.removeWebsiteAddress()(request)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the capture website page" in {
        redirectLocation(result) shouldBe Some(routes.CaptureWebsiteController.show().url)
      }
    }

    "the user is not authorised" should {

      "return 403" in {
        val result = {
          mockIndividualWithoutEnrolment()
          controller.removeWebsiteAddress()(requestWithValidationWebsiteKey)
        }

        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }
}
