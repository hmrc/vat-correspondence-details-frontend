/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.email

import assets.CustomerInfoConstants._
import common.SessionKeys._
import controllers.ControllerBaseSpec
import models.errors.ErrorModel
import play.api.http.Status
import play.api.http.Status._
import play.api.test.Helpers.{defaultAwaitTimeout, redirectLocation, session, status}
import utils.TestUtil

class BouncedEmailControllerSpec extends ControllerBaseSpec with TestUtil {

  object testController extends BouncedEmailController(mockErrorHandler, mockVatSubscriptionService)

  "The BouncedEmailController .show method" when {

    "called by a principal user" when {

      "the VAT subscription call is successful" when {

        "the call returns an email address that is verified" should {

          lazy val result = {
            mockIndividualAuthorised()
            mockGetCustomerInfo(user.vrn)(Right(fullCustomerInfoModel))
            testController.show(request)
          }

          "return 303" in {
            status(result) shouldBe SEE_OTHER
          }

          "redirect to VAT Overview" in {
            redirectLocation(result) shouldBe Some(mockConfig.vatOverviewUrl)
          }
        }

        "the call returns an email address that is unverified or the emailVerified field is not returned" should {

          lazy val result = {
            mockIndividualAuthorised()
            mockGetCustomerInfo(user.vrn)(Right(customerInfoEmailUnverified))
            testController.show(request)
          }

          "return 200 (OK)" in {
            status(result) shouldBe OK
          }

          "add the validation email session value" in {
            session(result).get(validationEmailKey) shouldBe Some("pepsimac@gmail.com")
          }
        }

        "the call doesn't return an email" should {

          lazy val result = {
            mockIndividualAuthorised()
            mockGetCustomerInfo(user.vrn)(Right(minCustomerInfoModel))
            testController.show(request)
          }

          "return 303" in {
            status(result) shouldBe SEE_OTHER
          }

          "redirect to VAT Overview" in {
            redirectLocation(result) shouldBe Some(mockConfig.vatOverviewUrl)
          }
        }
      }

      "the VAT subscription call is unsuccessful" should {

        lazy val result = {
          mockIndividualAuthorised()
          mockGetCustomerInfo(user.vrn)(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "")))
          testController.show(request)
        }

        "return 500 (ISE)" in {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "called by an agent" should {

      lazy val result = testController.show(agent)

      "return 401 (Unauthorized)" in {
        mockAgentAuthorised()
        status(result) shouldBe UNAUTHORIZED
      }
    }

  }

  "The BouncedEmailController .submit method" when {

    "called by a principal user" when {

      "the user's email is in session" when {

        "the form fails to bind" should {

          lazy val result = testController.submit(requestWithBadFormAndEmail)

          "return 400 (bad request)" in {
            mockIndividualAuthorised()
            status(result) shouldBe BAD_REQUEST
          }
        }

        "the form binds successfully" when {

          "the Verify Email option is bound" should {

            lazy val result = {
              mockIndividualAuthorised()
              testController.submit(requestWithValidationEmail.withFormUrlEncodedBody("yes_no" -> "yes"))
            }

            "return 303" in {
              status(result) shouldBe SEE_OTHER
            }

            "have the correct redirect location" in {
              redirectLocation(result) shouldBe Some(routes.VerifyPasscodeController.emailSendVerification.url)
            }

            "add the prepop email session value" in {
              session(result).get(prepopulationEmailKey) shouldBe Some(testEmail)
            }
          }

          "the Add Email option is bound" should {

            lazy val result = {
              mockIndividualAuthorised()
              testController.submit(requestWithValidationEmail.withFormUrlEncodedBody("yes_no" -> "no"))
            }

            "return 303" in {
              status(result) shouldBe SEE_OTHER
            }

            "have the correct redirect location" in {
              redirectLocation(result) shouldBe Some(routes.CaptureEmailController.show.url)
            }

          }
        }

      }

      "the user's email is not in session" should {

        lazy val result = {
          mockIndividualAuthorised()
          testController.submit(request.withFormUrlEncodedBody("yes_no" -> "no"))
        }

        "return ISE (500)" in {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "called by an agent" should {

      lazy val result = testController.submit(agent)

      "return 401 (Unauthorized)" in {
        mockAgentAuthorised()
        status(result) shouldBe UNAUTHORIZED
      }
    }
  }
}
