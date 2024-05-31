/*
 * Copyright 2024 HM Revenue & Customs
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

package assets

object EmailPrefMessages {

  val heading = "Do you want us to send you emails instead of letters?"
  val titleSuffixUser = " - Manage your VAT account - GOV.UK"
  val title: String = heading + titleSuffixUser
  val hint = "We currently send messages about your VAT by letter."
  val emailErrorMessage = "Select yes if you want communications by email"
  val yes = "Yes"
  val no = "No"
  val continue = "Continue"
  val errorTitlePrefix = "Error:"
  val errorHeading = "There is a problem"

}
