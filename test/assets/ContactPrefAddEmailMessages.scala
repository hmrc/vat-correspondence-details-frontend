/*
 * Copyright 2020 HM Revenue & Customs
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

object ContactPrefAddEmailMessages {

  val heading = "We do not have an email address for you"
  val titleSuffixUser = " - Business tax account - GOV.UK"
  val title: String = heading + titleSuffixUser
  val info1 = "Tell us your email address so we can change how we contact you."
  val info2 = "You’ll need to confirm the email address before we add it to your VAT account."
  val question = "Do you want to add an email address?"
  val yes = "Yes"
  val no = "No"
  val continue = "Continue"
  val errorTitlePrefix = "Error:"
  val errorHeading = "There is a problem"
  val errorMessage = "Select yes if you want to add an email address"
  val backLink = "Back"

}
