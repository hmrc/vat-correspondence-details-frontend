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

package forms

import config.ConfigKeys
import models.FeatureSwitchModel
import play.api.data.Form
import play.api.data.Forms._

object FeatureSwitchForm {
  val form: Form[FeatureSwitchModel] = Form(
    mapping(
      ConfigKeys.agentAccessFeature -> boolean,
      ConfigKeys.emailVerificationFeature -> boolean,
      ConfigKeys.stubContactPreferencesFeature -> boolean,
      ConfigKeys.contactPreferencesFeature -> boolean,
      ConfigKeys.languageSelectorFeature -> boolean,
      ConfigKeys.changeContactDetailsFeature -> boolean,
      ConfigKeys.emailVerifiedContactPrefFeature -> boolean,
      ConfigKeys.bulkPaperOffFeature -> boolean,
      ConfigKeys.btaEntryPointFeature -> boolean,
      ConfigKeys.letterToConfirmedEmailFeature -> boolean,
      ConfigKeys.contactPrefMigrationFeature -> boolean
    )(FeatureSwitchModel.apply)(FeatureSwitchModel.unapply)
  )
}
