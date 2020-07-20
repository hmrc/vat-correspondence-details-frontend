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
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import controllers.ControllerBaseSpec
import controllers.email.CaptureEmailController
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import uk.gov.hmrc.http.HeaderCarrier
import views.html.email.CaptureEmailView

import scala.concurrent.{ExecutionContext, Future}

class ChangeEmailControllerSpec extends ControllerBaseSpec{

  val testValidationEmail: String = "validation@example.com"
  val testValidEmail: String      = "pepsimac@gmail.com"
  val testInvalidEmail: String    = "invalidEmail"
  val view: CaptureEmailView = injector.instanceOf[CaptureEmailView]

  def setup(result: GetCustomerInfoResponse): Any =
    when(mockVatSubscriptionService.getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(result))

  def target(result: GetCustomerInfoResponse = Right(fullCustomerInfoModel)): CaptureEmailController = {
    setup(result)

    new CaptureEmailController(
      mockVatSubscriptionService,
      mockErrorHandler,
      mockAuditingService,
      view
    )
  }



}
