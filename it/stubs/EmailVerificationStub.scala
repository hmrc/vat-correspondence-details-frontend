/*
 * Copyright 2024 HM Revenue & Customs
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

package stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import helpers.WireMockMethods
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}

object EmailVerificationStub extends WireMockMethods {

  private val emailVerificationStateUri = "/email-verification/verified-email-check"
  private val emailVerificationRequestUri = "/email-verification/verification-requests"
  private val emailVerificationPasscodeRequestUri = "/email-verification/request-passcode"
  private val emailVerificationVerifyPasscodeUri = "/email-verification/verify-passcode"

  def stubEmailVerified(emailAddress: String): StubMapping =
    when(
      method = POST, uri = emailVerificationStateUri,
      body = Some(Json.obj("email" -> emailAddress).toString)
    ).thenReturn(status = OK, body = emailVerifiedResponseJson)

  def stubEmailNotVerified: StubMapping = when(method = POST, uri = emailVerificationStateUri)
    .thenReturn(status = NOT_FOUND, body = emailVerificationNotFoundJson)

  def stubEmailVerifiedError: StubMapping = when(method = POST, uri = emailVerificationStateUri)
    .thenReturn(status = INTERNAL_SERVER_ERROR, body = internalServerErrorJson)

  def stubVerificationRequestSent: StubMapping = when(method = POST, uri = emailVerificationRequestUri)
    .thenReturn(status = CREATED)

  def stubEmailAlreadyVerified: StubMapping = when(method = POST, uri = emailVerificationRequestUri)
    .thenReturn(status = CONFLICT)

  def stubVerificationRequestError: StubMapping = when(method = POST, uri = emailVerificationRequestUri)
    .thenReturn(status = INTERNAL_SERVER_ERROR, body = internalServerErrorJson)

  def stubVerifyPasscodeCreated: StubMapping = when(method = POST, uri = emailVerificationVerifyPasscodeUri)
    .thenReturn(status = CREATED)

  def stubVerifyPasscodeNotFound: StubMapping = when(method = POST, uri = emailVerificationVerifyPasscodeUri)
    .thenReturn(status = NOT_FOUND, body = verifyPasscodeNotFoundJson)

  def stubVerifyPasscodeUnexpected: StubMapping = when(method = POST, uri = emailVerificationVerifyPasscodeUri)
    .thenReturn(status = INTERNAL_SERVER_ERROR, body = internalServerErrorJson)

  def stubPasscodeAttemptsExceeded: StubMapping = when(method = POST, uri = emailVerificationVerifyPasscodeUri)
    .thenReturn(status = FORBIDDEN, body = tooManyAttempts)

  def stubIncorrectPasscode: StubMapping = when(method = POST, uri = emailVerificationVerifyPasscodeUri)
    .thenReturn(status = NOT_FOUND, body = incorrectPasscode)

  def stubPasscodeVerificationRequestSent: StubMapping = when(method = POST, uri = emailVerificationPasscodeRequestUri)
    .thenReturn(status = CREATED)

  def stubPasscodeEmailAlreadyVerified: StubMapping = when(method = POST, uri = emailVerificationPasscodeRequestUri)
    .thenReturn(status = CONFLICT, body = alreadyVerifiedJson)

  def stubPasscodeRequestError: StubMapping = when(method = POST, uri = emailVerificationPasscodeRequestUri)
    .thenReturn(status = INTERNAL_SERVER_ERROR, body = internalServerErrorJson)

  val emailVerifiedResponseJson: JsValue = Json.parse("""{"email": "scala@test.com"}""")

  val emailVerificationNotFoundJson: JsValue = Json.parse(
    """{
      |  "code": "NOT_VERIFIED",
      |  "message":"Email not verified."
      |}""".stripMargin
  )

  val internalServerErrorJson: JsValue = Json.parse(
    """{
      |  "code": "UNEXPECTED_ERROR",
      |  "message":"An unexpected error occurred."
      |}""".stripMargin
  )

  val alreadyVerifiedJson: JsValue = Json.parse(
    """{
      |  "code": "EMAIL_VERIFIED_ALREADY",
      |  "message":"Email already verified"
      |}""".stripMargin
  )

  val verifyPasscodeNotFoundJson: JsValue = Json.parse(
    """{
      |  "code": "PASSCODE_NOT_FOUND",
      |  "message": "Passcode not found"
      |}""".stripMargin
  )

  val tooManyAttempts: JsValue = Json.parse(
    """{
      |  "code": "MAX_PASSCODE_ATTEMPTS_EXCEEDED",
      |  "message": "Max attempts per session exceeded"
      |}""".stripMargin
  )

  val incorrectPasscode: JsValue = Json.parse(
    """{
      |  "code": "PASSCODE_MISMATCH",
      |  "message": "Incorrect passcode"
      |}""".stripMargin
  )
}
