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

package connectors.httpParsers

import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.LoggerUtil

object GetEmailVerificationStateHttpParser extends LoggerUtil {

  type GetEmailVerificationStateResponse = Either[GetEmailVerificationStateErrorResponse, EmailVerificationState]

  implicit object GetEmailVerificationStateHttpReads extends HttpReads[GetEmailVerificationStateResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetEmailVerificationStateResponse =
      response.status match {
        case OK => Right(EmailVerified)
        case NOT_FOUND =>
          logger.debug(
            "[GetEmailVerificationStateHttpParser][GetEmailVerificationStateHttpReads][read] - Email not verified"
          )
          Right(EmailNotVerified)
        case status =>
          logger.warn(
            s"[GetEmailVerificationStateHttpParser][GetEmailVerificationStateHttpReads][read] - " +
              s"Unexpected Response, Status $status returned, with response: ${response.body}"
          )
          Left(GetEmailVerificationStateErrorResponse(status, response.body))
      }
  }

  sealed trait EmailVerificationState

  case object EmailVerified extends EmailVerificationState

  case object EmailNotVerified extends EmailVerificationState

  case class GetEmailVerificationStateErrorResponse(status: Int, body: String)

}
