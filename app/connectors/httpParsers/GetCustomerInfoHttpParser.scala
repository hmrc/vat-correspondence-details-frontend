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

import models.errors.ErrorModel
import models.customerInformation.CustomerInformation
import play.api.Logger
import play.api.http.Status.{OK, INTERNAL_SERVER_ERROR}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object GetCustomerInfoHttpParser {

  type GetCustomerInfoResponse = Either[ErrorModel, CustomerInformation]

  implicit object CustomerInfoReads extends HttpReads[GetCustomerInfoResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetCustomerInfoResponse = {
      response.status match {
        case OK => response.json.validate[CustomerInformation].fold(
          invalid => {
            // $COVERAGE-OFF$
            Logger.warn(s"[GetCustomerInfoHttpParser][read] - Invalid JSON: $invalid")
            // $COVERAGE-ON$
            Left(ErrorModel(INTERNAL_SERVER_ERROR, "The endpoint returned invalid JSON."))
          },
          valid => {
            // $COVERAGE-OFF$
            Logger.debug(s"Successfully parsed the get customer info JSON: $valid")
            // $COVERAGE-ON$
            Right(valid)
          }
        )
        case status =>
          // $COVERAGE-OFF$
          Logger.warn(s"[GetCustomerInfoHttpParser][read]: Unexpected Response, Status $status returned,with " +
            s"response: ${response.body}")
          // $COVERAGE-ON$
          Left(ErrorModel(status, response.body))
      }
    }
  }
}
