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

import assets.CustomerInfoConstants._
import connectors.httpParsers.GetCustomerInfoHttpParser.{CustomerInfoReads, GetCustomerInfoResponse}
import models.errors.ErrorModel
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse
import utils.TestUtil

class GetCustomerInfoHttpParserSpec extends AnyWordSpecLike with Matchers with TestUtil {

  private def customerInfoResult(response: HttpResponse): GetCustomerInfoResponse =
    CustomerInfoReads.read("", "", response)

  "CustomerInfoReads" when {

    "the HTTP response status is 200 (OK)" when {

      "valid JSON is returned" should {

        "return a CustomerInformation model" in {
          val response = HttpResponse(Status.OK, fullCustomerInfoJson.toString)
          val result = customerInfoResult(response)
          result shouldBe Right(fullCustomerInfoModel)
        }
      }

      "invalid JSON is returned" should {

        "return an Error model with status code of 500 (INTERNAL_SERVER_ERROR)" in {
          val response = HttpResponse(Status.OK, Json.obj("fail" -> "nope").toString)
          val result = customerInfoResult(response)
          result shouldBe Left(invalidJsonError)
        }
      }
    }

    "the HTTP response status is not 200 (OK)" should {

      "return an Error model" in {
        val notFoundJson: JsValue = Json.obj(
          "code" -> "NOT_FOUND",
          "reason" -> "The requested information could not be found."
        )
        val response = HttpResponse(Status.NOT_FOUND, notFoundJson.toString)
        val result = customerInfoResult(response)
        result shouldBe Left(ErrorModel(Status.NOT_FOUND, Json.stringify(notFoundJson)))
      }
    }
  }
}
