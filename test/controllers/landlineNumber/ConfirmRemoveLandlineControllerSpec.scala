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

package controllers.landlineNumber

import assets.BaseTestConstants.testValidationLandline
import common.SessionKeys.{validationLandlineKey, prepopulationLandlineKey}
import controllers.ControllerBaseSpec
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.landlineNumber.ConfirmRemoveLandlineView

class ConfirmRemoveLandlineControllerSpec extends ControllerBaseSpec {

  val controller = new ConfirmRemoveLandlineController(inject[ConfirmRemoveLandlineView])
  val requestWithLandline: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(validationLandlineKey -> testValidationLandline)

  "Calling the show() action in ConfirmRemoveLandlineController" when {

    "there is a validation landline number in session" should {

      "return 200" in {
        val result = controller.show()(requestWithLandline)
        status(result) shouldBe Status.OK
      }
    }

    "there isn't a validation landline number in session" should {

      lazy val result = controller.show()(request)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the capture landline page" in {
        redirectLocation(result) shouldBe Some(routes.CaptureLandlineNumberController.show().url)
      }
    }

    "the user is not authorised" should {

      "return 403" in {
        val result = {
          mockIndividualWithoutEnrolment()
          controller.show()(request)
        }

        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }

  "Calling the removeLandlineNumber() action in ConfirmRemoveLandlineController" when {

    "there is a validation landline number in session" should {

      lazy val result = controller.removeLandlineNumber()(requestWithLandline)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the updateLandlineNumber() action in ConfirmLandlineNumberController" in {
        redirectLocation(result) shouldBe Some(routes.ConfirmLandlineNumberController.updateLandlineNumber().url)
      }

      "add a blank prepopulation landline to the session" in {
        session(result).get(prepopulationLandlineKey) shouldBe Some("")
      }
    }

    "there isn't a validation landline number in session" should {

      lazy val result = controller.removeLandlineNumber()(request)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the capture landline page" in {
        redirectLocation(result) shouldBe Some(routes.CaptureLandlineNumberController.show().url)
      }
    }

    "the user is not authorised" should {

      "return 403" in {
        val result = {
          mockIndividualWithoutEnrolment()
          controller.removeLandlineNumber()(request)
        }

        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }
}
