/*
 * Copyright 2019 HM Revenue & Customs
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

import play.api.http.Status.{CONFLICT, CREATED}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.LoggerUtil.{logDebug, logWarn}

object CreateEmailVerificationRequestHttpParser {

  type CreateEmailVerificationRequestResponse = Either[EmailVerificationRequestFailure, EmailVerificationRequestSuccess]

  implicit object CreateEmailVerificationRequestHttpReads extends HttpReads[CreateEmailVerificationRequestResponse] {
    override def read(method: String, url: String, response: HttpResponse): CreateEmailVerificationRequestResponse =
      response.status match {
        case CREATED =>
          logDebug("[CreateEmailVerificationRequestHttpReads][read] - Email request sent successfully")
          Right(EmailVerificationRequestSent)
        case CONFLICT =>
          logDebug("[CreateEmailVerificationRequestHttpReads][read] - Email already verified")
          Right(EmailAlreadyVerified)
        case status =>
          logWarn(
            "[CreateEmailVerificationRequestHttpParser][CreateEmailVerificationRequestHttpReads][read] - " +
            s"Failed to create email verification. Received status: $status Response body: ${response.body}"
          )
          Left(EmailVerificationRequestFailure(status, response.body))
    }
  }

  sealed trait EmailVerificationRequestSuccess

  object EmailAlreadyVerified extends EmailVerificationRequestSuccess

  object EmailVerificationRequestSent extends EmailVerificationRequestSuccess

  case class EmailVerificationRequestFailure(status: Int, body: String)
}
