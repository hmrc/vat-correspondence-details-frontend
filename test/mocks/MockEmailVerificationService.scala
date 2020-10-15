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

package mocks

import connectors.httpParsers.ResponseHttpParser.HttpGetResult
import connectors.httpParsers.VerifyPasscodeHttpParser.VerifyPasscodeRequest
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import services.EmailVerificationService
import uk.gov.hmrc.http.HeaderCarrier
import org.mockito.ArgumentMatchers

import scala.concurrent.Future

trait MockEmailVerificationService extends MockitoSugar with BeforeAndAfterEach {
  this: Suite =>

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockEmailVerificationService)
  }

  val mockEmailVerificationService: EmailVerificationService = mock[EmailVerificationService]

  def mockGetEmailVerificationState(emailAddress: String)(response: Future[Option[Boolean]]): Unit =
    when(mockEmailVerificationService
      .isEmailVerified(ArgumentMatchers.eq(emailAddress))(any[HeaderCarrier])) thenReturn response

  def mockCreateEmailVerificationRequest(response: Option[Boolean]): Unit =
    when(mockEmailVerificationService
      .createEmailVerificationRequest(any(), any())(any[HeaderCarrier])) thenReturn Future.successful(response)

  def mockVerifyPasscodeRequest(response: HttpGetResult[VerifyPasscodeRequest]): Unit =
    when(mockEmailVerificationService
      .verifyPasscode(any(), any())(any[HeaderCarrier])) thenReturn Future.successful(response)
}
