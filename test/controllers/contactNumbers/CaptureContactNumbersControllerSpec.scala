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

package controllers.contactNumbers

import assets.CustomerInfoConstants.fullCustomerInfoModel
import assets.BaseTestConstants._
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
import views.html.contactNumbers.CaptureContactNumbersView
import views.html.errors.NotFoundView

import scala.concurrent.ExecutionContext

class CaptureContactNumbersControllerSpec extends ControllerBaseSpec with MockVatSubscriptionService {

  val controller = new CaptureContactNumbersController(
    mockAuthPredicateComponents,
    mcc,
    mockVatSubscriptionService,
    mockErrorHandler,
    injector.instanceOf[CaptureContactNumbersView],
    injector.instanceOf[NotFoundView],
    mockConfig
  )

  "Calling the show action" when {

    "a user is enrolled with a valid enrolment" when {

      "the user's current landline and mobile are retrieved from session" should {

        lazy val result = controller.show(requestWithValidationPhoneNumbers)

        lazy val document = Jsoup.parse(bodyOf(result))

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

      lazy val result = controller.show(requestWithAllContactNumbers)
      lazy val document = Jsoup.parse(bodyOf(result))

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

      "prepopulate the form with the previously entered mobile" in {
        document.select("#mobileNumber").attr("value") shouldBe testPrepopMobile
      }

      "not call the VatSubscription service" in {
        verify(mockVatSubscriptionService, never())
          .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
      }
    }

    "there are no contact numbers in session" when {

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

        "prepopulate the form with the customerInfo landline result" in {
          document.select("#landlineNumber").attr("value") shouldBe "01234567890"
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

          lazy val result = controller.submit(requestWithValidationPhoneNumbers
            .withFormUrlEncodedBody("landlineNumber" -> testPrepopLandline, "mobileNumber" -> testPrepopMobile)
          )

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect to the confirm contact numbers controller" in {
            redirectLocation(result) shouldBe Some(routes.ConfirmContactNumbersController.show().url)
          }

          "add the new landline to the session" in {
            session(result).get(prepopulationLandlineKey) shouldBe Some(testPrepopLandline)
          }

          "add the new mobile to the session" in {
            session(result).get(prepopulationMobileKey) shouldBe Some(testPrepopMobile)
          }
        }

        //TODO implement when form validation is present
        "the form is submitted with errors" should {

//          lazy val result = controller.submit(requestWithContactNumbers
//            .withFormUrlEncodedBody("landlineNumber" -> , "mobileNumber" -> )
//          )
//
//          "return 400" in {
//            status(result) shouldBe Status.BAD_REQUEST
//          }
//
//          "return HTML" in {
//            contentType(result) shouldBe Some("text/html")
//            charset(result) shouldBe Some("utf-8")
//          }
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
