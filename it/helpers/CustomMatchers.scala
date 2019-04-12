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

package helpers

import org.jsoup.Jsoup
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.play.test.UnitSpec

trait CustomMatchers extends UnitSpec with GivenWhenThen{

  def httpStatus(expectedValue: Int): HavePropertyMatcher[WSResponse, Int] =
    new HavePropertyMatcher[WSResponse, Int] {

      def apply(response: WSResponse): HavePropertyMatchResult[Int] = {
        Then(s"the response status should be '$expectedValue'")
        HavePropertyMatchResult(
          response.status == expectedValue,
          "httpStatus",
          expectedValue,
          response.status
        )
      }
    }

  def pageTitle(expectedValue: String): HavePropertyMatcher[WSResponse, String] =
    new HavePropertyMatcher[WSResponse, String] {

      def apply(response: WSResponse): HavePropertyMatchResult[String] = {
        val body = Jsoup.parse(response.body)
        Then(s"the page title should be '$expectedValue'")
        HavePropertyMatchResult(
          body.title == expectedValue,
          "pageTitle",
          expectedValue,
          body.title
        )
      }
    }

  def elementText(cssSelector: String)(expectedValue: String): HavePropertyMatcher[WSResponse, String] =
    new HavePropertyMatcher[WSResponse, String] {

      def apply(response: WSResponse) = {
        val body = Jsoup.parse(response.body)
        Then(s"the text of '$cssSelector' should be '$expectedValue'")

        HavePropertyMatchResult(
          body.select(cssSelector).text == expectedValue,
          cssSelector,
          expectedValue,
          body.select(cssSelector).text
        )
      }
    }

  def redirectURI(expectedValue: String): HavePropertyMatcher[WSResponse, String] =
    new HavePropertyMatcher[WSResponse, String] {

      def apply(response: WSResponse): HavePropertyMatchResult[String] = {
        val redirectLocation: Option[String] = response.header("Location")
        Then(s"the redirect location should be '$expectedValue'")
        HavePropertyMatchResult(
          redirectLocation.contains(expectedValue),
          "redirectURI",
          expectedValue,
          redirectLocation.getOrElse("")
        )
      }
    }
}
