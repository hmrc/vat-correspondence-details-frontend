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

package connectors.httpParsers

import connectors.httpParsers.ResponseHttpParsers.HttpPostResult
import models.errors.{BadRequestError, ServerSideError, UnexpectedStatusError}
import play.api.http.Status.{BAD_REQUEST, OK}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object VatAgentClientLookupParser extends ResponseHttpParsers {

  implicit object VatAgentClientLookupParserReads extends HttpReads[HttpPostResult[Boolean]] {
    override def read(method: String, url: String, response: HttpResponse): HttpPostResult[Boolean] = {
      response.status match {
        case OK => Right(true)
        case BAD_REQUEST => Left(BadRequestError(response.status.toString, response.body))
        case status if status >= 500 && status < 600 => Left(ServerSideError(response.status.toString, response.body))
        case _ => Left(UnexpectedStatusError(response.status.toString, response.body))
      }
    }
  }
}
