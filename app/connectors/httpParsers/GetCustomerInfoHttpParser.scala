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

import models.customerInformation.CustomerInformation
import play.api.Logger
import play.api.http.Status
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object GetCustomerInfoHttpParser {

  case class GetCustomerInfoError(code: Int, body: String)

  type GetCustomerInfoResponse = Either[GetCustomerInfoError, CustomerInformation]

  implicit object CustomerInfoReads extends HttpReads[GetCustomerInfoResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetCustomerInfoResponse = {
      response.status match {
        case Status.OK => {
          Logger.debug("[CustomerCircumstancesHttpParser][read]: Status OK")
          response.json.validate[CustomerInformation].fold(
            invalid => {
              Logger.warn(s"[GetCustomerInfoHttpParser][read]: Invalid JSON: $invalid")
              Left(GetCustomerInfoError(Status.INTERNAL_SERVER_ERROR, "The endpoint returned invalid JSON."))
            },
            valid => Right(valid)
          )
        }
        case status =>
          Logger.warn(s"[GetCustomerInfoHttpParser][read]: Unexpected response. Status $status returned")
          Left(GetCustomerInfoError(response.status, response.body))
      }
    }
  }
}
