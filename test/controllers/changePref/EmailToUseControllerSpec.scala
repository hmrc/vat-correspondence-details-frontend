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

package controllers.changePref

import assets.CustomerInfoConstants.fullCustomerInfoModel
import common.SessionKeys
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import controllers.ControllerBaseSpec
import forms.YesNoForm.yesNo
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.changePref.EmailToUseView

import scala.concurrent.{ExecutionContext, Future}

class EmailToUseControllerSpec extends ControllerBaseSpec {

  def setup(result: GetCustomerInfoResponse): Any =
    when(mockVatSubscriptionService.getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(result))

  val testValidationEmail: String = "validation@example.com"

  lazy val existingEmailSessionRequest: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(SessionKeys.validationEmailKey -> testValidationEmail)

  lazy val noEmailSessionRequest: FakeRequest[AnyContentAsEmpty.type] =
    request

  val view: EmailToUseView = injector.instanceOf[EmailToUseView]

  def target(result: GetCustomerInfoResponse = Right(fullCustomerInfoModel)): EmailToUseController = {
    setup(result)

    new EmailToUseController(
      mockVatSubscriptionService,
      mockErrorHandler,
      view
    )
  }

  "Calling the show action in EmailToUseController" when {

    "the letterToConfirmedEmail switch is enabled" when {

      "there is an email in session" should {

        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          mockIndividualAuthorised()
          target().show()(existingEmailSessionRequest)
        }

        "return 200 (OK)" in {
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }

      "there isn't an email in session" should {

        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          mockIndividualAuthorised()
          target().show()(noEmailSessionRequest)
        }

        "return 200 (OK)" in {
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }

    "the letterToConfirmedEmail switch is disabled" should {

      "return a 400" in {

        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(false)
          mockIndividualAuthorised()
          target().show()(existingEmailSessionRequest)
        }
        status(result) shouldBe Status.BAD_REQUEST
      }
    }
  }

  "Calling the submit action in EmailToUseController" when {

    "the letterToConfirmedEmail switch is enabled" when {

      "the user submits after selecting an 'Yes' option" should {

        lazy val yesRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
          request
            .withFormUrlEncodedBody((yesNo, "yes"))
            .withSession(common.SessionKeys.validationEmailKey -> testValidationEmail)
        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          target().submit()(yesRequest)
        }

        "return 303 (SEE OTHER)" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        //TODO Update to correct location when journey is connected up
        s"Redirect to the '${controllers.email.routes.CaptureEmailController.show().url}'" in {
          redirectLocation(result) shouldBe Some(controllers.email.routes.CaptureEmailController.show().url)
        }
      }

      "the user submits after selecting an 'No' option" should {

        lazy val yesRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
          request
            .withFormUrlEncodedBody((yesNo, "no"))
            .withSession(common.SessionKeys.validationEmailKey -> testValidationEmail)
        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          target().submit()(yesRequest)
        }

        "return 303 (SEE OTHER)" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        s"Redirect to the '${controllers.email.routes.CaptureEmailController.show().url}'" in {
          redirectLocation(result) shouldBe Some(controllers.email.routes.CaptureEmailController.show().url)
        }
      }
    }

    "the letterToConfirmedEmail switch is disabled" when {

      lazy val yesRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
        request
          .withFormUrlEncodedBody((yesNo, "no"))
          .withSession(common.SessionKeys.validationEmailKey -> testValidationEmail)
      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(false)
        mockIndividualAuthorised()
        target().submit()(yesRequest)
      }

      "return a 400" in {
        status(result) shouldBe Status.BAD_REQUEST
      }
    }
  }
}