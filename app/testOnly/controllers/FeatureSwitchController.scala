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

package testOnly.controllers

import config.AppConfig
import forms.FeatureSwitchForm
import javax.inject.Inject
import controllers.BaseController
import models.FeatureSwitchModel
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import testOnly.views.html.FeatureSwitch

class FeatureSwitchController @Inject()(override val mcc: MessagesControllerComponents,
                                        featureSwitchView: FeatureSwitch,
                                        implicit val appConfig: AppConfig) extends BaseController(mcc) {

  def featureSwitch: Action[AnyContent] = Action { implicit request =>
    Ok(featureSwitchView(FeatureSwitchForm.form.fill(
      FeatureSwitchModel(
        agentAccess = appConfig.features.agentAccessEnabled(),
        emailVerification = appConfig.features.emailVerificationEnabled(),
        stubContactPreferences = appConfig.features.stubContactPreferences(),
        contactPreferences = appConfig.features.contactPreferencesEnabled(),
        languageSelector = appConfig.features.languageSelectorEnabled()
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
    Redirect(routes.FeatureSwitchController.featureSwitch())
  }
}
