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

package forms

import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.play.mappers.StopOnFirstFail
import uk.gov.hmrc.play.mappers.StopOnFirstFail.constraint

object WebsiteForm {

  val maxLength: Int = 132

  val websiteRegex: String =
    """^(((HTTP|http)(S|s)?\:\/\/((WWW|www)\.)?)|((
      |WWW|www)\.))?[a-zA-Z0-9\[_~\:\/?#\]@!&'()*+,
      |;=% ]+\.[a-zA-Z]{2,5}(\.[a-zA-Z]{2,5})?(\:[0-9]
      |{1,5})?(\/[a-zA-Z0-9_-]+(\/)?)*$""".stripMargin

  def websiteForm(website: String): Form[String] = Form(
    "website" -> text.verifying(
      StopOnFirstFail(
        constraint[String]("captureWebsite.error.empty", _.length != 0),
        constraint[String]("captureWebsite.error.notChanged", _.toLowerCase != website.toLowerCase),
        constraint[String]("captureWebsite.error.exceedsMaxLength", _.length <= maxLength),
        constraint[String]("captureWebsite.error.invalid", _.matches(websiteRegex))
      )
    )
  )
}
