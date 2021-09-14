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

package connectors.httpParsers

import connectors.httpParsers.VerifyPasscodeHttpParser._
import connectors.httpParsers.VerifyPasscodeHttpParser.VerifyPasscodeHttpReads.read
import models.errors.ErrorModel
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status._
import uk.gov.hmrc.http.HttpResponse

class VerifyPasscodeHttpParserSpec extends AnyWordSpecLike with Matchers {

  "VerifyPasscodeHttpReads" when {

    "the response status is CREATED (201)" should {

      "return a SuccessfullyVerified object" in {
        val httpResponse: HttpResponse = HttpResponse(CREATED, "")
        read("", "", httpResponse) shouldBe Right(SuccessfullyVerified)
      }
    }

    "the response status is NO_CONTENT (204)" should {

      "return an AlreadyVerified object" in {
        val httpResponse: HttpResponse = HttpResponse(NO_CONTENT, "")
        read("", "", httpResponse) shouldBe Right(AlreadyVerified)
      }
    }

    "the response status is FORBIDDEN (403)" should {

      "return an TooManyAttempts object" in {
        val httpResponse: HttpResponse =
          HttpResponse(FORBIDDEN, """{"code":"MAX_PASSCODE_ATTEMPTS_EXCEEDED","message":"Max attempts per session exceeded"}""")
        read("", "", httpResponse) shouldBe Right(TooManyAttempts)
      }
    }

    "the response status is NOT_FOUND (404)" when {

      "the response body contains 'PASSCODE_NOT_FOUND'" should {

        "return a PasscodeNotFound object" in {
          val httpResponse: HttpResponse =
            HttpResponse(NOT_FOUND, """{"code":"PASSCODE_NOT_FOUND","message":"Passcode not found"}""")
          read("", "", httpResponse) shouldBe Right(PasscodeNotFound)
        }
      }

      "the response body contains 'PASSCODE_MISMATCH'" should {

        "return an AlreadyVerified object" in {
          val httpResponse: HttpResponse =
            HttpResponse(NOT_FOUND, """{"code":"PASSCODE_MISMATCH","message":"Incorrect passcode"}""")
          read("", "", httpResponse) shouldBe Right(IncorrectPasscode)
        }
      }
    }

    "the response status is something unexpected" should {

      "return an error model with the status and response body" in {
        val httpResponse : HttpResponse = HttpResponse(INTERNAL_SERVER_ERROR, "Err0r")
        read("", "", httpResponse) shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, "Err0r"))
      }
    }
  }
}
