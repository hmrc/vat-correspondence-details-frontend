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

package forms.validation.utils

import play.api.data.Forms._
import play.api.data._

object MappingUtil {

  val optText: Mapping[Option[String]] = optional(text)

  implicit class OTextUtil(mapping: Mapping[Option[String]]) {

    def toText: Mapping[String] =
      mapping.transform(
        x => x.getOrElse(""),
        x => Some(x)
      )

    def toBoolean: Mapping[Boolean] =
      mapping.transform(
        {
          case Some("true") => true
          case _ => false
        },
        x => Some(x.toString)
      )
  }
}
