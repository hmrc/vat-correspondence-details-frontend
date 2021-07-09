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

package controllers.contactPreference

import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.YesNoForm
import javax.inject.{Inject, Singleton}
import models.{No, Yes, YesNo}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import views.html.contactPreference.AddEmailAddressView

import scala.concurrent.Future

@Singleton
class AddEmailAddressController @Inject()(val errorHandler: ErrorHandler,
                                          addEmailAddressView: AddEmailAddressView)
                                         (implicit val appConfig: AppConfig,
                                          authComps: AuthPredicateComponents,
                                          inFlightComps: InFlightPredicateComponents) extends BaseController {

  val formYesNo: Form[YesNo] = YesNoForm.yesNoForm("cPrefAddEmail.error")

  def show: Action[AnyContent] = (contactPreferencePredicate andThen paperPrefPredicate andThen inFlightContactPrefPredicate).async { implicit user =>
      Future.successful(Ok(addEmailAddressView(formYesNo)))
  }

  def submit: Action[AnyContent] = (contactPreferencePredicate andThen paperPrefPredicate andThen inFlightContactPrefPredicate) { implicit user =>
    formYesNo.bindFromRequest().fold (
      formWithErrors =>
        BadRequest(addEmailAddressView(formWithErrors)),
      {
        case Yes => Redirect(controllers.email.routes.CaptureEmailController.showPrefJourney())
        case No => Redirect(appConfig.btaAccountDetailsUrl)
      }
    )
  }
}