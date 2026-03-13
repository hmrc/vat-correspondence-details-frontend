/*
 * Copyright 2026 HM Revenue & Customs
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

package models

object PhoneNumber {

  def isValid(input: String): Boolean = {
    val stripped = input.replace(" ", "")

    val localNumber       = """^0\d{6,23}$""".r
    val internationalPlus = """^\+(?!00)(?!440)\d{6,23}$""".r
    val internationalZero = """^00(?!440)\d{5,22}$""".r

    val isLocal             = localNumber.matches(stripped)
    val isInternationalPlus = internationalPlus.matches(stripped)
    val isInternationalZero = internationalZero.matches(stripped)

    isLocal || isInternationalPlus || isInternationalZero
  }
}
