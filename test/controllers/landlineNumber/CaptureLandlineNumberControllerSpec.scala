/*
 * Copyright 2023 HM Revenue & Customs
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

import assets.BaseTestConstants._
import audit.models.ChangeLandlineNumberStartAuditModel
import common.SessionKeys._
import controllers.ControllerBaseSpec
import mocks.MockVatSubscriptionService
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.test.Helpers._
import views.html.landlineNumber.CaptureLandlineNumberView

class CaptureLandlineNumberControllerSpec extends ControllerBaseSpec with MockVatSubscriptionService {

  val controller = new CaptureLandlineNumberController(
    mockVatSubscriptionService,
    mockErrorHandler,
    mockAuditingService,
    inject[CaptureLandlineNumberView]
  )

  "Calling the show action" when {

    "a user is enrolled with a valid enrolment" when {

      "the user's current landline is retrieved from session" should {

        lazy val result = controller.show(getRequestWithValidationLandlineNumber)

        lazy val document = Jsoup.parse(contentAsString(result))

        "return 200" in {
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "prepopulate the form with the user's current landline" in {
          document.select("#landlineNumber").attr("value") shouldBe testValidationLandline
        }

        "audit the correct information for the journey start event" in {
          verifyExtendedAudit(ChangeLandlineNumberStartAuditModel(Some(testValidationLandline), vrn, None))
        }
      }
    }

    "the previous form value is retrieved from session" should {

      lazy val result = controller.show(getRequestWithAllLandlineNumbers)
      lazy val document = Jsoup.parse(contentAsString(result))

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "prepopulate the form with the previously entered landline" in {
        document.select("#landlineNumber").attr("value") shouldBe testPrepopLandline
      }

      "audit the correct information for the journey start event" in {
        verifyExtendedAudit(ChangeLandlineNumberStartAuditModel(Some(testValidationLandline), vrn, None))
      }
    }

    "the user has no current landline number (inflight predicate has set a blank string in session)" should {

      lazy val result = controller.show(getRequest.withSession(validationLandlineKey -> ""))
      lazy val document = Jsoup.parse(contentAsString(result))

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "not prepopulate the form" in {
        document.select("#landlineNumber").attr("value") shouldBe ""
      }

      "audit the correct information for the journey start event" in {
        verifyExtendedAudit(ChangeLandlineNumberStartAuditModel(None, vrn, None))
      }
    }

    "there is no landline number in session" should {

      lazy val result = controller.show(getRequest)

      "return 500" in {
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "a user is does not have a valid enrolment" should {

      lazy val result = controller.show(getRequest)

      "return 403" in {
        mockIndividualWithoutEnrolment
        status(result) shouldBe Status.FORBIDDEN
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "a user is not logged in" should {

      lazy val result = controller.show(getRequest)

      "return 401" in {
        mockMissingBearerToken
        status(result) shouldBe Status.UNAUTHORIZED
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "a user is an agent" should {

      lazy val result = {
        mockAgentAuthorised
        controller.show(fakeGetRequestWithSessionKeys)
      }

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    insolvencyCheck(controller.show())
  }

  "Calling the submit action" when {

    "a user is enrolled with a valid enrolment" when {

      "there are contact numbers in session" when {

        "the form is successfully submitted" should {

          lazy val result = controller.submit(postRequestWithValidationLandlineNumber
            .withFormUrlEncodedBody("landlineNumber" -> testPrepopLandline)
          )

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the confirm contact numbers controller" in {
            redirectLocation(result) shouldBe Some(routes.ConfirmLandlineNumberController.show.url)
          }

          "add the new landline to the session" in {
            session(result).get(prepopulationLandlineKey) shouldBe Some(testPrepopLandline)
          }
        }

        "the form is submitted with errors" should {

          lazy val result = controller.submit(postRequestWithAllLandlineNumbers
            .withFormUrlEncodedBody("landlineNumber" -> "")
          )

          "return 400" in {
            status(result) shouldBe Status.BAD_REQUEST
          }

          "return HTML" in {
            contentType(result) shouldBe Some("text/html")
            charset(result) shouldBe Some("utf-8")
          }
        }
      }

      "there are no contact numbers in session" when {

        lazy val result = controller.submit(postRequest)

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }

    "a user is does not have a valid enrolment" should {

      lazy val result = controller.submit(postRequest)

      "return 403" in {
        mockIndividualWithoutEnrolment
        status(result) shouldBe Status.FORBIDDEN
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "a user is not logged in" should {

      lazy val result = controller.submit(postRequest)

      "return 401" in {
        mockMissingBearerToken
        status(result) shouldBe Status.UNAUTHORIZED
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    insolvencyCheck(controller.submit())
  }
}
