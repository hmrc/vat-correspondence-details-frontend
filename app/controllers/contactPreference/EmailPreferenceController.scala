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

package controllers.contactPreference

import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.YesNoForm
import javax.inject.Inject
import models.contactPreferences.ContactPreference.paper
import models.{No, Yes, YesNo}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import views.html.contactPreference.EmailPreferenceView

import scala.concurrent.Future

class EmailPreferenceController @Inject()(errorHandler: ErrorHandler,
                                          emailPreferenceView: EmailPreferenceView)
                                         (implicit val appConfig: AppConfig,
                                          authComps: AuthPredicateComponents,
                                          inFlightPredicateComponents: InFlightPredicateComponents) extends BaseController {

  val formYesNo: Form[YesNo] = YesNoForm.yesNoForm("emailPreference.error")

  def show: Action[AnyContent] = (contactPreferencePredicate andThen paperPrefPredicate).async { implicit user =>
    if(appConfig.features.letterToConfirmedEmailEnabled()) {
      Future.successful(Ok(emailPreferenceView(formYesNo))
        .removingFromSession(SessionKeys.contactPrefUpdate)
        .addingToSession(SessionKeys.currentContactPrefKey -> paper))
    } else {
      Future.successful(NotFound(errorHandler.notFoundTemplate))
    }
  }

  def submit: Action[AnyContent] = (contactPreferencePredicate andThen paperPrefPredicate).async { implicit user =>
    if(appConfig.features.letterToConfirmedEmailEnabled()) {
      formYesNo.bindFromRequest().fold (
        formWithErrors => Future.successful(BadRequest(emailPreferenceView(formWithErrors))),
        {
          case Yes => Future.successful(Redirect(controllers.contactPreference.routes.EmailToUseController.show())
            .addingToSession(SessionKeys.contactPrefUpdate -> "true"))
          case No => Future.successful(Redirect(appConfig.btaAccountDetailsUrl))
        }
      )
    } else {
      Future.successful(NotFound(errorHandler.notFoundTemplate))
    }
  }
}
