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

package controllers.email

import audit.models.ContactPreferenceAuditModel
import common.SessionKeys._
import controllers.ControllerBaseSpec
import mocks.{MockAuditingService, MockContactPreferenceService}
import models.contactPreferences.ContactPreference
import models.errors.ErrorModel
import org.mockito.Mockito.reset
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.templates.ChangeSuccessView
import assets.BaseTestConstants.vrn
import scala.concurrent.Future

class EmailChangeSuccessControllerSpec extends ControllerBaseSpec with MockContactPreferenceService with MockAuditingService {

  val view: ChangeSuccessView = inject[ChangeSuccessView]

  object TestController extends EmailChangeSuccessController(
    mockAuditingService,
    mockContactPreferenceService,
    mockVatSubscriptionService,
    view
  )

  val successfulChangeRequest: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(emailChangeSuccessful -> "true")

  "Calling the show action" when {

    "a user is enrolled with a valid enrolment" when {

      "the user has the email change success key in the session" when {

        "a valid response is retrieved from the contact preference service" should {

          "a digital preference is retrieved" should {

            lazy val result = {
              mockIndividualAuthorised()
              getMockContactPreference(vrn)(Right(ContactPreference("DIGITAL")))
              mockGetEmailVerificationStatus(Some(true))
              TestController.show(successfulChangeRequest)
            }

            "return 200" in {
              status(result) shouldBe Status.OK
            }

            "audit the contact preference" in {
              verifyExtendedAudit(ContactPreferenceAuditModel(vrn, "DIGITAL"))
              reset(mockAuditingService)
            }
          }

          "a paper preference is retrieved" should {

            lazy val result = {
              mockIndividualAuthorised()
              getMockContactPreference(vrn)(Future(Right(ContactPreference("PAPER"))))
              mockGetEmailVerificationStatus(None)
              TestController.show(successfulChangeRequest)
            }

            "return 200" in {
              status(result) shouldBe Status.OK
            }

            "audit the contact preference" in {
              verifyExtendedAudit(ContactPreferenceAuditModel(vrn, "PAPER"))
              reset(mockAuditingService)
            }
          }
        }

        "an invalid response is retrieved from the contact preference service" should {

          lazy val result = {
            mockIndividualAuthorised()
            getMockContactPreference(vrn)(Future(Left(ErrorModel(Status.BAD_GATEWAY, "Error"))))
            mockGetEmailVerificationStatus(None)
            TestController.show(successfulChangeRequest)
          }

          "return 200" in {
            status(result) shouldBe Status.OK
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
            charset(result) shouldBe Some("utf-8")
          }
        }
      }

      "the user does not have the email change success key in the session" should {

        lazy val result = {
          mockIndividualAuthorised()
          TestController.show(request)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to the capture email page" in {
          redirectLocation(result) shouldBe Some(controllers.email.routes.CaptureEmailController.show().url)
        }
      }
    }

    "a user is does not have a valid enrolment" should {

      lazy val result = {
        mockIndividualWithoutEnrolment()
        TestController.show(request)
      }

      "return 403" in {
        status(result) shouldBe Status.FORBIDDEN
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }
}
