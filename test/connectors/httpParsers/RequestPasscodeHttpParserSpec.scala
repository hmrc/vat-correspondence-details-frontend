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

import connectors.httpParsers.RequestPasscodeHttpParser.{EmailIsAlreadyVerified, EmailVerificationPasscodeRequestSent}
import connectors.httpParsers.RequestPasscodeHttpParser.RequestPasscodeHttpReads.read
import models.errors.ErrorModel
import play.api.http.Status.{CONFLICT, CREATED, INTERNAL_SERVER_ERROR}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec

class RequestPasscodeHttpParserSpec extends UnitSpec {

  "RequestPasscodeHttpReads" when {

    "the response status is CREATED" should {

      "return an EmailVerificationPasscodeRequestSent object" in {
        val httpResponse: HttpResponse = HttpResponse(CREATED,"")
        read("", "", httpResponse) shouldBe Right(EmailVerificationPasscodeRequestSent)
      }
    }

    "the response status is CONFLICT" should {

      "return an EmailIsAlreadyVerified object" in {
        val httpResponse: HttpResponse = HttpResponse(CONFLICT,"")
        read("", "", httpResponse) shouldBe Right(EmailIsAlreadyVerified)
      }
    }

    "the response returns an unexpected status" should {

      "return an error model with the status and response body" in {
        val httpResponse : HttpResponse = HttpResponse(INTERNAL_SERVER_ERROR,"")
        read("", "", httpResponse) shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, httpResponse.body))
      }
    }
  }

}