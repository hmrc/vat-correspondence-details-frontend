/*
 * Copyright 2021 HM Revenue & Customs
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

import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.{reset, when}
import connectors.EmailVerificationConnector
import uk.gov.hmrc.http.HeaderCarrier
import connectors.httpParsers.CreateEmailVerificationRequestHttpParser.CreateEmailVerificationRequestResponse
import connectors.httpParsers.GetEmailVerificationStateHttpParser.GetEmailVerificationStateResponse
import connectors.httpParsers.RequestPasscodeHttpParser.EmailVerificationPasscodeRequest
import connectors.httpParsers.ResponseHttpParser.HttpPostResult
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any

import scala.concurrent.Future

trait MockEmailVerificationConnector extends MockitoSugar with BeforeAndAfterEach {
  this: Suite =>

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockEmailVerificationConnector)
  }

  val mockEmailVerificationConnector: EmailVerificationConnector = mock[EmailVerificationConnector]

  def mockGetEmailVerificationState(emailAddress: String)(response: Future[GetEmailVerificationStateResponse]): Unit =
    when(mockEmailVerificationConnector.getEmailVerificationState(
      ArgumentMatchers.eq(emailAddress)
    )(ArgumentMatchers.any[HeaderCarrier])) thenReturn response

  def mockCreateEmailVerificationRequest(emailAddress: String, continueUrl: String)(response: Future[CreateEmailVerificationRequestResponse]): Unit =
    when(mockEmailVerificationConnector.createEmailVerificationRequest(
      ArgumentMatchers.eq(emailAddress),
      ArgumentMatchers.eq(continueUrl)
    )(ArgumentMatchers.any[HeaderCarrier])) thenReturn response

  def mockRequestEmailPasscode(response: Future[HttpPostResult[EmailVerificationPasscodeRequest]]): Unit =
    when(mockEmailVerificationConnector.requestEmailPasscode(any[String], any[String])(any[HeaderCarrier])) thenReturn response
}
