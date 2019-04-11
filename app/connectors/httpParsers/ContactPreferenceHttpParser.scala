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

import connectors.httpParsers.ResponseHttpParser.HttpGetResult
import models.contactPreferences.ContactPreference
import models.contactPreferences.ContactPreference._
import models.errors.ErrorModel
import play.api.Logger
import play.api.http.Status
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object ContactPreferenceHttpParser {

  implicit object ContactPreferenceReads extends HttpReads[HttpGetResult[ContactPreference]] {

    override def read(method: String, url: String, response: HttpResponse): HttpGetResult[ContactPreference] = {

      response.status match {
        case Status.OK => {
          //$COVERAGE-OFF$
          Logger.debug("[ContactPreferencesHttpParser][read]: Status OK")
          //$COVERAGE-ON$
          response.json.validate[ContactPreference].fold(
            invalid => {
              //$COVERAGE-OFF$
              Logger.debug(s"[ContactPreferencesHttpParser][read]: Invalid Json - $invalid")
              Logger.warn(s"[ContactPreferencesHttpParser][read]: Invalid Json received from Contact Preferences")
              //$COVERAGE-ON$
              Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "Invalid Json received from Contact Preferences"))
            },
            valid => valid.preference.toUpperCase match {
              case `digital` | `paper` => Right(valid)
              case _ =>
                //$COVERAGE-OFF$
                Logger.warn(s"[ContactPreferencesHttpParser][read]: Invalid preference type received from Contact Preferences")
                //$COVERAGE-ON$
                Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "Invalid preference type received from Contact Preferences"))
            }
          )
        }
        case status =>
          //$COVERAGE-OFF$
          Logger.warn(s"[ContactPreferencesHttpParser][read]: Unexpected Response, Status: $status, Response: ${response.body}")
          //$COVERAGE-ON$
          Left(ErrorModel(status, s"Unexpected Response. Response: ${response.body}"))
      }
    }
  }

}
