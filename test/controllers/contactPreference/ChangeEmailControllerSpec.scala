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

package controllers.contactPreference

import assets.CustomerInfoConstants.fullCustomerInfoModel
import common.SessionKeys
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import controllers.ControllerBaseSpec
import models.errors.ErrorModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import play.api.http.Status
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.email.CaptureEmailView

import scala.concurrent.{ExecutionContext, Future}

class ChangeEmailControllerSpec extends ControllerBaseSpec {

  val testValidationEmail: String = "validation@example.com"
  val testValidEmail: String = "pepsimac@gmail.com"
  val testInvalidEmail: String = "invalidEmail"
  val view: CaptureEmailView = injector.instanceOf[CaptureEmailView]

  def setup(result: GetCustomerInfoResponse): Any =
    when(mockVatSubscriptionService.getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(result))

  def target(result: GetCustomerInfoResponse = Right(fullCustomerInfoModel)): ChangeEmailController = {
    setup(result)

    new ChangeEmailController(
      mockVatSubscriptionService,
      mockErrorHandler,
      mockAuditingService,
      view
    )
  }

  "Calling the show action" when {

    "the letterToConfirmedEmail feature switch is turned on" when {

      "a user is enrolled with a valid enrolment" when {

        "there is an email in session" when {

          "the validation email is retrieved from session" should {

            lazy val result ={
              mockConfig.features.letterToConfirmedEmailEnabled(true)
              target().show(request
                .withSession(common.SessionKeys.validationEmailKey -> testValidationEmail))
            }
            lazy val document = Jsoup.parse(bodyOf(result))

            "return 200" in {
              status(result) shouldBe Status.OK
            }

            "return HTML" in {
              contentType(result)(defaultTimeout) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }

            "prepopulate the form with the validation email" in {
              document.select("input").attr("value") shouldBe testValidationEmail
            }
            "not call the VatSubscription service" in {
              verify(mockVatSubscriptionService, never())
                .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
            }
          }
          "the previous form value is retrieved from session" should {

            lazy val result = {
              mockConfig.features.letterToConfirmedEmailEnabled(true)
              target().show(request.withSession(
                common.SessionKeys.validationEmailKey -> testValidationEmail,
                common.SessionKeys.prepopulationEmailKey -> testValidEmail)
              )
            }
            lazy val document = Jsoup.parse(bodyOf(result))

            "return 200" in {
              status(result) shouldBe Status.OK
            }

            "return HTML" in {
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }

            "prepopulate the form with the previously entered form value" in {
              document.select("input").attr("value") shouldBe testValidEmail
            }

            "not call the VatSubscription service" in {
              verify(mockVatSubscriptionService, never())
                .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
            }
          }
        }

        "there is no email in session" when {

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
              document.select("input").attr("value") shouldBe testValidEmail
            }
          }

          "the customerInfo call fails" should {

            lazy val result = target(Left(ErrorModel(
              Status.NOT_FOUND,
              "error"
            ))).show(request)

            "return 404" in {
              status(result) shouldBe Status.INTERNAL_SERVER_ERROR
            }

            "return HTML" in {
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }
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

      "the inflight predicate is not mocked out and there is nothing in session" should {

        lazy val inflightTarget = {

          setup(Right(fullCustomerInfoModel))
          new ChangeEmailController(
            mockVatSubscriptionService,
            mockErrorHandler,
            mockAuditingService,
            view
          )
        }

        lazy val result = inflightTarget.show(request)

        "return 200" in {
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }
    " letterToConfirmedEmail feature switch is off" should {
      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(false)
        target().show(request
          .withSession(common.SessionKeys.validationEmailKey -> testValidationEmail))
      }
      "return NOT FOUND" in {
        status(result) shouldBe Status.NOT_FOUND
      }

    }
  }

  "Calling the submit action" when {

    "the letterToConfirmedEmail feature switch is turned on" when {

      "a user is enrolled with a valid enrolment" when {

        "there is an email in session" when {

          "the form is successfully submitted" should {

            lazy val result = {
              mockConfig.features.letterToConfirmedEmailEnabled(true)
              target().submit(request
                .withFormUrlEncodedBody("email" -> testValidEmail)
                .withSession(common.SessionKeys.validationEmailKey -> testValidationEmail))
            }
            "redirect to the confirm email view" in {
              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) shouldBe Some("")
            }

            "add the new email to the session" in {
              session(result).get(SessionKeys.prepopulationEmailKey) shouldBe Some(testValidEmail)
            }
          }

          "the form is unsuccessfully submitted" should {

            lazy val result = {
              mockConfig.features.letterToConfirmedEmailEnabled(true)
              target().submit(request
                .withFormUrlEncodedBody("email" -> testInvalidEmail)
                .withSession(common.SessionKeys.validationEmailKey -> testValidationEmail))
            }

            "reload the page with errors" in {
              status(result) shouldBe Status.BAD_REQUEST
            }

            "return HTML" in {
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }
          }
        }

        "there is no email in session" when {

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
    " letterToConfirmedEmail feature switch is off" should {
      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(false)
        target().show(request
          .withSession(common.SessionKeys.validationEmailKey -> testValidationEmail))
      }
      "return NOT FOUND" in {
        status(result) shouldBe Status.NOT_FOUND
      }

    }
  }
}
