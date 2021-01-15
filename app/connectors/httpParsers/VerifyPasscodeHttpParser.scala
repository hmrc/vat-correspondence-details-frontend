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

package connectors.httpParsers

import connectors.httpParsers.ResponseHttpParser.HttpPostResult
import models.errors.ErrorModel
import play.api.Logger
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object VerifyPasscodeHttpParser {

  sealed trait VerifyPasscodeRequest

  object SuccessfullyVerified extends VerifyPasscodeRequest
  object AlreadyVerified extends VerifyPasscodeRequest
  object TooManyAttempts extends VerifyPasscodeRequest
  object PasscodeNotFound extends VerifyPasscodeRequest
  object IncorrectPasscode extends VerifyPasscodeRequest

  implicit object VerifyPasscodeHttpReads extends HttpReads[HttpPostResult[VerifyPasscodeRequest]] {
    override def read(method: String, url: String, response: HttpResponse): HttpPostResult[VerifyPasscodeRequest] =
      response.status match {
        case CREATED =>
          Logger.debug("[VerifyPasscodeHttpParser][VerifyPasscodeHttpReads][read] - Email successfully verified")
          Right(SuccessfullyVerified)
        case NO_CONTENT =>
          Logger.debug("[VerifyPasscodeHttpParser][VerifyPasscodeHttpReads][read] - Email is already verified")
          Right(AlreadyVerified)
        case FORBIDDEN =>
          Logger.debug("[VerifyPasscodeHttpParser][VerifyPasscodeHttpReads][read] - Max attempts per session exceeded")
          Right(TooManyAttempts)
        case NOT_FOUND if response.body.contains("PASSCODE_NOT_FOUND") =>
          Logger.warn("[VerifyPasscodeHttpParser][VerifyPasscodeHttpReads][read] - Passcode not found (or expired) for this email")
          Right(PasscodeNotFound)
        case NOT_FOUND if response.body.contains("PASSCODE_MISMATCH") =>
          Logger.debug("[VerifyPasscodeHttpParser][VerifyPasscodeHttpReads][read] - Incorrect passcode")
          Right(IncorrectPasscode)
        case status =>
          Logger.warn(
            "[VerifyPasscodeHttpParser][VerifyPasscodeHttpReads][read] - " +
              s"Received unexpected error. Response status: $status, Response body: ${response.body}"
          )
          Left(ErrorModel(status, response.body))
      }
  }
}
