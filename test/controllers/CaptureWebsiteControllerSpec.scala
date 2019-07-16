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

import common.SessionKeys
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import models.customerInformation.{ContactDetails, CustomerInformation, PPOB, PPOBAddress}
import models.errors.ErrorModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import play.api.http.Status
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.CaptureWebsiteView
import assets.CustomerInfoConstants.fullCustomerInfoModel

import scala.concurrent.{ExecutionContext, Future}

class CaptureWebsiteControllerSpec extends ControllerBaseSpec {

  val testValidationWebsite: String = "https://www.current-valid-website.com"
  val testValidWebsite: String = "https://www.new-valid-website.com"
  val testInvalidWebsite: String = "invalid@£$%^&website"
  val view: CaptureWebsiteView = injector.instanceOf[CaptureWebsiteView]

  def setup(result: GetCustomerInfoResponse): Any =
    when(mockVatSubscriptionService.getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(result))

  def target(result: GetCustomerInfoResponse = Right(fullCustomerInfoModel)): CaptureWebsiteController = {
    setup(result)

    new CaptureWebsiteController(
      mockAuthPredicateComponents,
      mockInflightPPOBPredicate,
      mcc,
      mockVatSubscriptionService,
      mockErrorHandler,
      mockAuditingService,
      view,
      mockConfig
    )
  }


  "Calling the show action" when {

    "a user is enrolled with a valid enrolment" when {

      "the user's current website is retrieved from session" should {

        lazy val result = target().show(request.withSession(
          common.SessionKeys.validationWebsiteKey -> testValidationWebsite)
        )

        lazy val document = Jsoup.parse(bodyOf(result))

        "return 200" in {
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "prepopulate the form with the user's current website" in {
          document.select("#website").attr("value") shouldBe testValidationWebsite
        }

        "not call the VatSubscription service" in {
          verify(mockVatSubscriptionService, never())
            .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
        }

      }
    }

    "the previous form value is retrieved from session" should {

      lazy val result = target().show(request.withSession(
        common.SessionKeys.validationWebsiteKey -> testValidationWebsite,
        common.SessionKeys.prepopulationWebsiteKey -> testValidWebsite)
      )
      lazy val document = Jsoup.parse(bodyOf(result))

      "return 200" in {
        status(result) shouldBe Status.OK
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "prepopulate the form with the previously entered form value" in {
        document.select("#website").attr("value") shouldBe testValidWebsite
      }

      "not call the VatSubscription service" in {
        verify(mockVatSubscriptionService, never())
          .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
      }
    }

    "there is no website in session" when {

      "the customerInfo call succeeds" should {

        lazy val result = target().show(request)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return 200" in {
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "prepopulate the form with the customerInfo result" in {
          document.select("#website").attr("value") shouldBe "www.pepsi-mac.biz"
        }
      }

      "the customerInfo call fails" should {

        lazy val result = target(Left(ErrorModel(
          Status.INTERNAL_SERVER_ERROR,
          "error"
        ))).show(request)

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

      lazy val result = target().show(request)

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

      lazy val result = target().show(request)

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

  "Calling the submit action" when {

    "a user is enrolled with a valid enrolment" when {

      "there is a website in session" when {

        "the form is successfully submitted" should {

          lazy val result = target().submit(request
            .withFormUrlEncodedBody("website" -> testValidWebsite)
            .withSession(common.SessionKeys.validationWebsiteKey -> testValidationWebsite))

          "redirect to the confirm website view" in {
            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.ConfirmWebsiteController.show().url)
          }

          "add the new website to the session" in {
            session(result).get(SessionKeys.prepopulationWebsiteKey) shouldBe Some(testValidWebsite)
          }
        }

//        "the form is unsuccessfully submitted" should {
//
//          lazy val result = target().submit(request
//            .withFormUrlEncodedBody("website" -> testInvalidWebsite)
//            .withSession(common.SessionKeys.validationWebsiteKey -> testValidationWebsite))
//
//          "reload the page with errors" in {
//            status(result) shouldBe Status.BAD_REQUEST
//          }
//
//          "return HTML" in {
//            contentType(result) shouldBe Some("text/html")
//            charset(result) shouldBe Some("utf-8")
//          }
//        }
      }

      "there is no website in session" when {

        lazy val result = target().submit(request)

        "render the error view" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }

    "a user is does not have a valid enrolment" should {

      lazy val result = target().submit(request)

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

      lazy val result = target().submit(request)

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
