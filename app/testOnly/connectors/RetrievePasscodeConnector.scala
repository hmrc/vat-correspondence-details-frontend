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

package testOnly.connectors

import config.AppConfig
import connectors.httpParsers.ResponseHttpParser.HttpGetResult
import javax.inject.Inject
import models.errors.ErrorModel
import play.api.Logger
import play.api.http.Status
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}
import testOnly.models.Passcode

import scala.concurrent.{ExecutionContext, Future}

class RetrievePasscodeConnector @Inject()(val http: HttpClient,
                                          val config: AppConfig) {

  val passcodeUrl: String = config.emailVerificationBaseUrl + "/test-only/passcodes"

  implicit object Reads extends HttpReads[HttpGetResult[Passcode]] {
    override def read (method: String, url: String, response: HttpResponse): HttpGetResult[Passcode] = {
      response.status match {
        case Status.OK => Right(response.json.as[Passcode])
        case status => Logger.warn("[RetrievePasscodeConnector][read] - Failed to retrieve passcode. " +
          s"Received status: $status. Received body: ${response.body}")
          Left(ErrorModel(status, response.body))
      }
    }
  }
  def getPasscode(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpGetResult[Passcode]] = http.GET(passcodeUrl)
}

