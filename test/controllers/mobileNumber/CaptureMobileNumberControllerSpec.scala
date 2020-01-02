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

import assets.BaseTestConstants._
import assets.CustomerInfoConstants.fullCustomerInfoModel
import common.SessionKeys._
import controllers.ControllerBaseSpec
import mocks.MockVatSubscriptionService
import models.errors.ErrorModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify}
import play.api.http.Status
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.mobileNumber.CaptureMobileNumberView

import scala.concurrent.ExecutionContext

class CaptureMobileNumberControllerSpec extends ControllerBaseSpec with MockVatSubscriptionService {

  val controller = new CaptureMobileNumberController(
    mockVatSubscriptionService,
    mockErrorHandler,
    inject[CaptureMobileNumberView]
  )

  "Calling the show action" when {

    "a user is enrolled with a valid enrolment" when {

      "the user's current mobile is retrieved from session" should {

        lazy val result = controller.show(requestWithValidationMobileNumber)

        lazy val document = Jsoup.parse(bodyOf(result))

        "return 200" in {
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "prepopulate the form with the user's current mobile" in {
          document.select("#mobileNumber").attr("value") shouldBe testValidationMobile
        }

        "not call the VatSubscription service" in {
          verify(mockVatSubscriptionService, never())
            .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
        }

      }
    }

    "the previous form value is retrieved from session" should {

      lazy val result = controller.show(requestWithAllMobileNumbers)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "prepopulate the form with the previously entered mobile" in {
        document.select("#mobileNumber").attr("value") shouldBe testPrepopMobile
      }

      "not call the VatSubscription service" in {
        verify(mockVatSubscriptionService, never())
          .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
      }
    }

    "there is no mobile number in session" when {

      "the customerInfo call succeeds" should {

        lazy val result = {
          mockGetCustomerInfo(vrn)(Right(fullCustomerInfoModel))
          controller.show(request)
        }
        lazy val document = Jsoup.parse(bodyOf(result))

        "return 200" in {
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "prepopulate the form with the customerInfo mobile result" in {
          document.select("#mobileNumber").attr("value") shouldBe "07707707707"
        }
      }

      "the customerInfo call fails" should {

        lazy val result = {
          mockGetCustomerInfo("999999999")(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "error")))
          controller.show(request)
        }

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

      lazy val result = controller.show(request)

      "return 403" in {
        mockIndividualWithoutEnrolment()
        status(result) shouldBe Status.FORBIDDEN
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "a user is not logged in" should {

      lazy val result = controller.show(request)

      "return 401" in {
        mockMissingBearerToken()
        status(result) shouldBe Status.UNAUTHORIZED
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "a user is an agent" should {

      lazy val result = {
        mockAgentAuthorised()
        mockGetCustomerInfo(vrn)(Right(fullCustomerInfoModel))
        controller.show(fakeRequestWithClientsVRN)
      }

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "the contact details feature switch is disabled" should {

      lazy val result = {
        mockConfig.features.changeContactDetailsEnabled(false)
        controller.show(request)
      }

      "return 404" in {
        status(result) shouldBe Status.NOT_FOUND
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }

  "Calling the submit action" when {

    "a user is enrolled with a valid enrolment" when {

      "there are contact numbers in session" when {

        "the form is successfully submitted" should {

          lazy val result = controller.submit(requestWithValidationMobileNumber
            .withFormUrlEncodedBody("mobileNumber" -> testPrepopMobile)
          )

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the confirm contact numbers controller" in {
            redirectLocation(result) shouldBe Some(routes.ConfirmMobileNumberController.show().url)
          }

          "add the new mobile to the session" in {
            session(result).get(prepopulationMobileKey) shouldBe Some(testPrepopMobile)
          }
        }

        "the form is submitted with errors" should {

          lazy val result = controller.submit(requestWithAllMobileNumbers
            .withFormUrlEncodedBody("mobileNumber" -> "")
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

        lazy val result = controller.submit(request)

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

      lazy val result = controller.submit(request)

      "return 403" in {
        mockIndividualWithoutEnrolment()
        status(result) shouldBe Status.FORBIDDEN
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    "a user is not logged in" should {

      lazy val result = controller.submit(request)

      "return 401" in {
        mockMissingBearerToken()
        status(result) shouldBe Status.UNAUTHORIZED
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }
}
