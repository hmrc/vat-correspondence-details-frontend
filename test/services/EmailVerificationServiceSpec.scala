/*
 * Copyright 2018 HM Revenue & Customs
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

package services

import java.util.UUID
import connectors.httpParsers.CreateEmailVerificationRequestHttpParser.{EmailAlreadyVerified, EmailVerificationRequestFailure, EmailVerificationRequestSent}
import connectors.httpParsers.GetEmailVerificationStateHttpParser.{EmailNotVerified, EmailVerified, GetEmailVerificationStateErrorResponse}
import mocks.MockEmailVerificationConnector
import org.scalatest.EitherValues
import play.api.http.Status._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmailVerificationServiceSpec extends UnitSpec with MockEmailVerificationConnector with TestUtil with EitherValues {

  object TestStoreEmailService extends EmailVerificationService(
    mockEmailVerificationConnector,
    mockConfig
  )

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
  private lazy val continueUrl = mockConfig.emailVerificationBaseUrl
  val testEmail: String = UUID.randomUUID().toString

  "Create Email verifications request" when {
    "the email verification is sent successfully" should {
      "return a StoreEmailSuccess with an emailVerified of false" in {
        mockCreateEmailVerificationRequest(testEmail, continueUrl)(Future.successful(Right(EmailVerificationRequestSent)))

        val res: Option[Boolean] = await(TestStoreEmailService.createEmailVerificationRequest(testEmail, continueUrl))

        res shouldBe Some(true)
      }
    }

    "the email address has already been verified" should {
      "return a StoreEmailSuccess with an emailVerified of true" in {
        mockCreateEmailVerificationRequest(testEmail, continueUrl)(Future.successful(Right(EmailAlreadyVerified)))

        val res: Option[Boolean] = await(TestStoreEmailService.createEmailVerificationRequest(testEmail, continueUrl))

        res shouldBe Some(false)
      }
    }

    "the email address verification request failed" should {
      "return an EmailVerificationFailure" in {
        mockCreateEmailVerificationRequest(testEmail, continueUrl)(Future.successful(Left(EmailVerificationRequestFailure(BAD_REQUEST, ""))))

        val res: Option[Boolean] = await(TestStoreEmailService.createEmailVerificationRequest(testEmail, continueUrl))

        res shouldBe None
      }
    }
  }


  "Check Email verifications status request" when {
    "the email verified" should {
      "return Some(true)" in {
        mockGetEmailVerificationState(testEmail)(Future.successful(Right(EmailVerified)))

        val res: Option[Boolean] = await(TestStoreEmailService.isEmailVerified(testEmail))

        res shouldBe Some(true)
      }
    }

    "the email is not verified" should {
      "return Some(false)" in {
        mockGetEmailVerificationState(testEmail)(Future.successful(Right(EmailNotVerified)))

        val res: Option[Boolean] = await(TestStoreEmailService.isEmailVerified(testEmail))

        res shouldBe Some(false)
      }
    }
    "the email is check failed" should {
      "return None" in {
        mockGetEmailVerificationState(testEmail)(Future.successful(Left(GetEmailVerificationStateErrorResponse(BAD_REQUEST, ""))))

        val res: Option[Boolean] = await(TestStoreEmailService.isEmailVerified(testEmail))

        res shouldBe None
      }
    }
  }

}
