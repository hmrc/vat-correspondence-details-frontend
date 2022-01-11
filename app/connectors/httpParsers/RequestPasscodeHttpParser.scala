/*
 * Copyright 2022 HM Revenue & Customs
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


import connectors.httpParsers.ResponseHttpParser.HttpGetResult
import models.errors.ErrorModel
import utils.LoggerUtil
import play.api.http.Status.{CONFLICT, CREATED}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object RequestPasscodeHttpParser extends LoggerUtil {

  implicit object RequestPasscodeHttpReads extends HttpReads[HttpGetResult[EmailVerificationPasscodeRequest]] {
    override def read(method: String, url: String, response: HttpResponse): HttpGetResult[EmailVerificationPasscodeRequest] =
      response.status match {
        case CREATED =>
          logger.debug("[RequestPasscodeHttpParser][RequestPasscodeHttpReads][read] - Email passcode request sent successfully")
          Right(EmailVerificationPasscodeRequestSent)
        case CONFLICT =>
          logger.debug("[RequestPasscodeHttpParser][RequestPasscodeHttpReads][read] - Email already verified")
          Right(EmailIsAlreadyVerified)
        case status =>
          logger.warn(
            "[RequestPasscodeHttpParser][RequestPasscodeHttpReads][read] - " +
              s"Failed to create email verification passcode. Received status: $status, Response body: ${response.body}"
          )
          Left(ErrorModel(status, response.body))
      }
  }

  sealed trait EmailVerificationPasscodeRequest

  object EmailIsAlreadyVerified extends EmailVerificationPasscodeRequest

  object EmailVerificationPasscodeRequestSent extends EmailVerificationPasscodeRequest

}