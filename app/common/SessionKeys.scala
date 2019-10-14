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

package common

object SessionKeys {
  val clientVrn: String = "CLIENT_VRN"
  val inFlightContactDetailsChangeKey: String = "inFlightContactDetailsChange"
  val verifiedAgentEmail: String = "verifiedAgentEmail"

  val validationEmailKey: String = "vatCorrespondenceValidationEmail"
  val prepopulationEmailKey: String = "vatCorrespondencePrepopulationEmail"
  val emailChangeSuccessful: String = "vatCorrespondenceEmailChangeSuccessful"

  val validationWebsiteKey: String = "vatCorrespondenceValidationWebsite"
  val prepopulationWebsiteKey: String = "vatCorrespondencePrepopulationWebsite"
  val websiteChangeSuccessful: String = "vatCorrespondenceWebsiteChangeSuccessful"

  val validationLandlineKey: String = "vatCorrespondenceValidationLandline"
  val prepopulationLandlineKey: String = "vatCorrespondencePrepopulationLandline"
  val landlineChangeSuccessful: String = "vatCorrespondenceLandlineChangeSuccessful"

  val validationMobileKey: String = "vatCorrespondenceValidationMobile"
  val prepopulationMobileKey: String = "vatCorrespondencePrepopulationMobile"
  val mobileChangeSuccessful: String = "vatCorrespondenceMobileChangeSuccessful"
}
