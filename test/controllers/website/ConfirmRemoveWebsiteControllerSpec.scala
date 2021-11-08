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

package controllers.website

import common.SessionKeys
import controllers.ControllerBaseSpec
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.website.ConfirmRemoveWebsiteView

class ConfirmRemoveWebsiteControllerSpec extends ControllerBaseSpec  {

  lazy val controller = new ConfirmRemoveWebsiteController(injector.instanceOf[ConfirmRemoveWebsiteView])

  lazy val requestWithValidationWebsiteKey: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(SessionKeys.validationWebsiteKey -> testWebsite)

  "Calling the show action in ConfirmRemoveWebsiteController" when {

    "there is a website address in session" should {

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

    insolvencyCheck(controller.show)
  }

  "Calling the removeWebsiteAddress() action in ConfirmRemoveWebsiteController" when {

    "there is a validation website address in session" when {

      "the form has errors" should {

        lazy val result = controller.removeWebsiteAddress()(requestWithValidationWebsiteKey)

        "return 400" in {
          status(result) shouldBe Status.BAD_REQUEST
        }
      }

      "the form is submitted successfully" when {

        "the Yes option is submitted" should {

          lazy val result = controller.removeWebsiteAddress()(requestWithValidationWebsiteKey
            .withFormUrlEncodedBody("yes_no" -> "yes"))

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

        "the No option is submitted" should {

          lazy val result = controller.removeWebsiteAddress()(requestWithValidationWebsiteKey
            .withFormUrlEncodedBody("yes_no" -> "no"))

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the business details page" in {
            redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
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

        insolvencyCheck(controller.removeWebsiteAddress())
      }
    }
    }
  }
