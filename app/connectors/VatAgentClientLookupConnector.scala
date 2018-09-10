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

package connectors

import config.AppConfig
import javax.inject.Inject
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import scala.concurrent.{ExecutionContext, Future}
import connectors.httpParsers.ResponseHttpParsers.HttpPostResult
import play.api.Logger
import javax.inject.Singleton

@Singleton
class VatAgentClientLookupConnector @Inject()(val http: HttpClient,
                                              val appConfig: AppConfig) {

  private lazy val setupUrl: String = appConfig.vatAgentClientLookupServiceUrl + appConfig.vatAgentClientLookupServicePath

  def setupJourney(redirectUrlJson: JsValue)(implicit hc: HeaderCarrier,
                                             ec: ExecutionContext): Future[HttpPostResult[Boolean]] = {

    import connectors.httpParsers.VatAgentClientLookupParser.VatAgentClientLookupParserReads

    Logger.debug(s"[VatAgentClientLookupConnector][setupJourney]: Posting vat-correspondence redirectUrl to " +
      s"vat-agent-client-lookup-frontend - $redirectUrlJson")

    Logger.info(s"[VatAgentClientLookupConnector][setupJourney]: Posting vat-correspondence redirectUrl to " +
      s"vat-agent-client-lookup-frontend")

    http.POST(setupUrl, redirectUrlJson) map {
      case success@Right(_) => success
      case httpError@Left(_) => httpError
    }
  }
}
