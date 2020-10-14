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

package connectors

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import connectors.httpParsers.CreateEmailVerificationRequestHttpParser.{CreateEmailVerificationRequestResponse, EmailAlreadyVerified, EmailVerificationRequestFailure, EmailVerificationRequestSent}
import connectors.httpParsers.GetEmailVerificationStateHttpParser._
import connectors.httpParsers.RequestPasscodeHttpParser.{EmailIsAlreadyVerified, EmailVerificationPasscodeRequest, EmailVerificationPasscodeRequestSent}
import connectors.httpParsers.ResponseHttpParser.HttpPostResult
import helpers.IntegrationBaseSpec
import models.errors.ErrorModel
import play.api.http.Status.INTERNAL_SERVER_ERROR
import stubs.EmailVerificationStub
import uk.gov.hmrc.http.HeaderCarrier

class EmailVerificationConnectorISpec extends IntegrationBaseSpec {

  private trait Test {
    def setupStubs(): StubMapping
    val connector: EmailVerificationConnector = app.injector.instanceOf[EmailVerificationConnector]
    implicit val hc: HeaderCarrier = HeaderCarrier()
  }

  private val testEmail = "scala@gmail.com"
  private val testContinueUrl = "/home"


  "Calling getEmailVerificationState" when {

    "the email is verified" should {

      "return an EmailVerified response" in new Test {
        override def setupStubs(): StubMapping = EmailVerificationStub.stubEmailVerified(testEmail)
        setupStubs()
        val expected = Right(EmailVerified)
        val result: GetEmailVerificationStateResponse = await(connector.getEmailVerificationState(testEmail))

        result shouldBe expected
      }
    }

    "the email is not verified" should {

      "return an EmailNotVerified response" in new Test {
        override def setupStubs(): StubMapping = EmailVerificationStub.stubEmailNotVerified
        setupStubs()
        val expected = Right(EmailNotVerified)
        val result: GetEmailVerificationStateResponse = await(connector.getEmailVerificationState(testEmail))

        result shouldBe expected
      }
    }

    "the endpoint returns an unexpected status" should {

      "return a GetEmailVerificationStateErrorResponse" in new Test {
        override def setupStubs(): StubMapping = EmailVerificationStub.stubEmailVerifiedError
        setupStubs()
        val expected = Left(GetEmailVerificationStateErrorResponse(
          INTERNAL_SERVER_ERROR,
          EmailVerificationStub.internalServerErrorJson.toString
        ))
        val result: GetEmailVerificationStateResponse = await(connector.getEmailVerificationState(testEmail))

        result shouldBe expected
      }
    }
  }

  "Calling createEmailVerificationRequest" when {

    "the post is successful" should {

      "return an EmailVerificationRequestSent" in new Test {
        override def setupStubs(): StubMapping = EmailVerificationStub.stubVerificationRequestSent
        setupStubs()
        val expected = Right(EmailVerificationRequestSent)
        val result: CreateEmailVerificationRequestResponse =
          await(connector.createEmailVerificationRequest(testEmail, testContinueUrl))

        result shouldBe expected
      }
    }

    "the email is already verified" should {

      "return an EmailAlreadyVerified" in new Test {
        override def setupStubs(): StubMapping = EmailVerificationStub.stubEmailAlreadyVerified
        setupStubs()
        val expected = Right(EmailAlreadyVerified)
        val result: CreateEmailVerificationRequestResponse =
          await(connector.createEmailVerificationRequest(testEmail, testContinueUrl))

        result shouldBe expected
      }
    }

    "the endpoint returns an unexpected status" should {

      "return an EmailVerificationRequestSent" in new Test {
        override def setupStubs(): StubMapping = EmailVerificationStub.stubVerificationRequestError
        setupStubs()
        val expected = Left(EmailVerificationRequestFailure(
          INTERNAL_SERVER_ERROR,
          EmailVerificationStub.internalServerErrorJson.toString
        ))
        val result: CreateEmailVerificationRequestResponse =
          await(connector.createEmailVerificationRequest(testEmail, testContinueUrl))

        result shouldBe expected
      }
    }
  }

  "Calling requestEmailPasscode" when {

    "the call is successful" should {

      "return an EmailVerificationPasscodeRequestSent" in new Test {
        override def setupStubs(): StubMapping = EmailVerificationStub.stubPasscodeVerificationRequestSent

        setupStubs()
        val expected = Right(EmailVerificationPasscodeRequestSent)
        val result: HttpPostResult[EmailVerificationPasscodeRequest] = await(connector.requestEmailPasscode(testEmail, "en"))

        result shouldBe expected
      }
    }

    "the email is already verified" should {

      "return an EmailIsAlreadyVerified" in new Test {
        override def setupStubs(): StubMapping = EmailVerificationStub.stubPasscodeEmailAlreadyVerified

        setupStubs()
        val expected = Right(EmailIsAlreadyVerified)
        val result: HttpPostResult[EmailVerificationPasscodeRequest] = await(connector.requestEmailPasscode(testEmail, "en"))

        result shouldBe expected
      }
    }

    "the endpoint returns an unexpected status" should {

      "return an Error" in new Test {
        override def setupStubs(): StubMapping = EmailVerificationStub.stubPasscodeRequestError

        setupStubs()
        val expected = Left(ErrorModel(
          INTERNAL_SERVER_ERROR,
          EmailVerificationStub.internalServerErrorJson.toString
        ))
        val result: HttpPostResult[EmailVerificationPasscodeRequest] = await(connector.requestEmailPasscode(testEmail, "en"))

        result shouldBe expected
      }
    }
  }
}
