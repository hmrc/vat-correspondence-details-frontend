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

package forms

import play.api.data.Form

package object prevalidation {

  import TrimOption._
  import CaseOption._

  val defaultTrims: Map[String, TrimOption] = Map[String, TrimOption]()
  val defaultCases: Map[String, CaseOption] = Map[String, CaseOption]()

  val trimAllFunc: String => String = (value: String) => value.replaceAll("[\\s]", "")
  val trimBothFunc: String => String = (value: String) => value.trim
  val trimBothAndCompressFunc: String => String = (value: String) => value.trim.replaceAll("[\\s]{2,}", " ")

  def PreprocessedForm[T](validation: Form[T],
                          trimRules: Map[String, TrimOption] = defaultTrims,
                          caseRules: Map[String, CaseOption] = defaultCases): PrevalidationAPI[T] = {

    val trules = trimRules
    val crules = caseRules

    new PrevalidationAPI[T] {
      override val form: Form[T] = validation
      override val trimRules: Map[String, TrimOption] = trules
      override val caseRules: Map[String, CaseOption] = crules
    }
  }
}
