/*
 * Copyright 2021 HM Revenue & Customs
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

package testOnly.controllers

import config.AppConfig
import forms.FeatureSwitchForm
import javax.inject.Inject
import models.FeatureSwitchModel
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import testOnly.views.html.FeatureSwitch
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

class FeatureSwitchController @Inject()(val mcc: MessagesControllerComponents,
                                        featureSwitchView: FeatureSwitch,
                                        implicit val appConfig: AppConfig) extends FrontendController(mcc) {

  def featureSwitch: Action[AnyContent] = Action { implicit request =>
    Ok(featureSwitchView(FeatureSwitchForm.form.fill(
      FeatureSwitchModel(
        agentAccess = appConfig.features.agentAccessEnabled(),
        emailVerification = appConfig.features.emailVerificationEnabled(),
        stubContactPreferences = appConfig.features.stubContactPreferences(),
        contactPreferences = appConfig.features.contactPreferencesEnabled(),
        languageSelector = appConfig.features.languageSelectorEnabled(),
        changeContactDetails = appConfig.features.changeContactDetailsEnabled(),
        emailVerifiedContactPref = appConfig.features.emailVerifiedContactPrefEnabled(),
        bulkPaperOff = appConfig.features.bulkPaperOffEnabled(),
        btaEntryPoint = appConfig.features.btaEntryPointEnabled(),
        letterToConfirmedEmail = appConfig.features.letterToConfirmedEmailEnabled(),
        contactPrefMigration = appConfig.features.contactPrefMigrationEnabled()
      )
    )))
  }

  def submitFeatureSwitch: Action[AnyContent] = Action { implicit request =>
    FeatureSwitchForm.form.bindFromRequest().fold(
      _ => Redirect(routes.FeatureSwitchController.featureSwitch()),
      success = handleSuccess
    )
  }

  def handleSuccess(model: FeatureSwitchModel): Result = {
    appConfig.features.agentAccessEnabled(model.agentAccess)
    appConfig.features.emailVerificationEnabled(model.emailVerification)
    appConfig.features.stubContactPreferences(model.stubContactPreferences)
    appConfig.features.contactPreferencesEnabled(model.contactPreferences)
    appConfig.features.languageSelectorEnabled(model.languageSelector)
    appConfig.features.changeContactDetailsEnabled(model.changeContactDetails)
    appConfig.features.emailVerifiedContactPrefEnabled(model.emailVerifiedContactPref)
    appConfig.features.bulkPaperOffEnabled(model.bulkPaperOff)
    appConfig.features.btaEntryPointEnabled(model.btaEntryPoint)
    appConfig.features.letterToConfirmedEmailEnabled(model.letterToConfirmedEmail)
    appConfig.features.contactPrefMigrationEnabled(model.contactPrefMigration)
    Redirect(routes.FeatureSwitchController.featureSwitch())
  }
}
