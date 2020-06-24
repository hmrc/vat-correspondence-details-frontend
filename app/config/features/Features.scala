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

package config.features

import config.ConfigKeys
import javax.inject.Inject
import play.api.Configuration

class Features @Inject()(config: Configuration) {

  val agentAccessEnabled = new Feature(ConfigKeys.agentAccessFeature, config)
  val emailVerificationEnabled = new Feature(ConfigKeys.emailVerificationFeature, config)
  val stubContactPreferences = new Feature(ConfigKeys.stubContactPreferencesFeature, config)
  val contactPreferencesEnabled = new Feature(ConfigKeys.contactPreferencesFeature, config)
  val languageSelectorEnabled = new Feature(ConfigKeys.languageSelectorFeature, config)
  val changeContactDetailsEnabled = new Feature(ConfigKeys.changeContactDetailsFeature, config)
  val emailVerifiedContactPrefEnabled = new Feature(ConfigKeys.emailVerifiedContactPrefFeature, config)
  val bulkPaperOffEnabled = new Feature(ConfigKeys.bulkPaperOffFeature, config)
  val btaEntryPointEnabled = new Feature(ConfigKeys.btaEntryPointFeature, config)
  val letterToConfirmedEmailEnabled = new Feature(ConfigKeys.letterToConfirmedEmailFeature, config)
}
