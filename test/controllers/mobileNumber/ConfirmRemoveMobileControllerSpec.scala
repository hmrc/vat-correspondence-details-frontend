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

package controllers.mobileNumber

import assets.BaseTestConstants.testValidationMobile
import common.SessionKeys.{prepopulationMobileKey, validationMobileKey}
import controllers.ControllerBaseSpec
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.mobileNumber.ConfirmRemoveMobileView

class ConfirmRemoveMobileControllerSpec extends ControllerBaseSpec {

  val controller = new ConfirmRemoveMobileController(inject[ConfirmRemoveMobileView])
  val requestWithMobile: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(validationMobileKey -> testValidationMobile)

  "Calling the show() action in ConfirmRemoveMobileController" when {

    "there is a validation mobile number in session" should {

      "return 200" in {
        val result = controller.show()(requestWithMobile)
        status(result) shouldBe Status.OK
      }
    }

    "there isn't a validation mobile number in session" should {

      lazy val result = controller.show()(request)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the capture mobile page" in {
        redirectLocation(result) shouldBe Some(routes.CaptureMobileNumberController.show().url)
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

  "Calling the removeMobileNumber() action in ConfirmRemoveMobileController" when {

    "there is a validation mobile number in session" should {

      lazy val result = controller.removeMobileNumber()(requestWithMobile)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the updateMobileNumber() action in ConfirmMobileNumberController" in {
        redirectLocation(result) shouldBe Some(routes.ConfirmMobileNumberController.updateMobileNumber().url)
      }

      "add a blank prepopulation mobile to the session" in {
        session(result).get(prepopulationMobileKey) shouldBe Some("")
      }
    }

    "there isn't a validation mobile number in session" should {

      lazy val result = controller.removeMobileNumber()(request)

      "return 303" in {
        status(result) shouldBe Status.SEE_OTHER
      }

      "redirect to the capture mobile page" in {
       redirectLocation(result) shouldBe Some(routes.CaptureMobileNumberController.show().url)
      }
    }

    "the user is not authorised" should {

      "return 403" in {
        val result = {
          mockIndividualWithoutEnrolment()
          controller.removeMobileNumber()(request)
        }

        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }
}
