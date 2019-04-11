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

import connectors.httpParsers.ContactPreferenceHttpParser.ContactPreferenceReads
import models.contactPreferences.ContactPreference
import models.errors.ErrorModel
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import utils.TestUtil

class ContactPreferenceHttpParserSpec extends TestUtil {

  "The ContactPreferenceHttpParser" when {

    "the http response status is OK" when {

      "preference is Digital" should {

        val response = Json.obj("preference" -> "DIGITAL")

        "return Digital" in {
          ContactPreferenceReads.read("", "", HttpResponse(Status.OK, Some(response))) shouldBe Right(ContactPreference("DIGITAL"))
        }
      }

      "preference is Paper" should {

        val response = Json.obj("preference" -> "PAPER")

        "return Paper" in {
          ContactPreferenceReads.read("", "", HttpResponse(Status.OK, Some(response))) shouldBe Right(ContactPreference("PAPER"))
        }
      }

      "preference is of various cases" should {

        val response = Json.obj("preference" -> "digITaL")

        "return DIGITAL" in {
          ContactPreferenceReads.read("", "", HttpResponse(Status.OK, Some(response))) shouldBe Right(ContactPreference("digITaL"))
        }
      }

      "preference is invalid" should {

        val response = Json.obj("preference" -> "Invalid")

        "return an ErrorModel" in {
          ContactPreferenceReads.read("", "", HttpResponse(Status.OK, Some(response))) shouldBe
            Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "Invalid preference type received from Contact Preferences"))
        }
      }

      "json is invalid" should {

        val response = Json.obj("Invalid" -> "Invalid")

        "return an ErrorModel" in {
          ContactPreferenceReads.read("", "", HttpResponse(Status.OK, Some(response))) shouldBe
            Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "Invalid Json received from Contact Preferences"))
        }
      }
    }

    "the http response status is unexpected" should {

      "return an ErrorModel" in {
        ContactPreferenceReads.read("", "", HttpResponse(Status.NOT_FOUND, responseString = Some("Response body"))) shouldBe
          Left(ErrorModel(Status.NOT_FOUND, "Unexpected Response. Response: Response body"))
      }
    }
  }
}
