/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.mobileNumber

import common.SessionKeys.{prepopulationMobileKey, validationMobileKey}
import config.AppConfig
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.YesNoForm
import javax.inject.Inject
import models.{No, Yes, YesNo}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import views.html.mobileNumber.ConfirmRemoveMobileView

class ConfirmRemoveMobileController @Inject()(val confirmRemoveMobile: ConfirmRemoveMobileView)
                                             (implicit appConfig: AppConfig,
                                              authComps: AuthPredicateComponents,
                                              inFlightComps: InFlightPredicateComponents) extends BaseController {

  val yesNoForm: Form[YesNo] = YesNoForm.yesNoForm("confirmRemoveMobile.error")

  def show(): Action[AnyContent] = (allowAgentPredicate andThen inFlightMobileNumberPredicate) { implicit user =>
    user.session.get(validationMobileKey).filter(_.nonEmpty) match {
      case Some(_) =>
        Ok(confirmRemoveMobile(yesNoForm))
      case None =>
        Redirect(routes.CaptureMobileNumberController.show())
    }
  }

  def removeMobileNumber(): Action[AnyContent] = (allowAgentPredicate andThen
                                                  inFlightMobileNumberPredicate) { implicit user =>
    user.session.get(validationMobileKey).filter(_.nonEmpty) match {
      case Some(_) =>
        yesNoForm.bindFromRequest.fold(
          errorForm => {
            BadRequest(confirmRemoveMobile(errorForm))
          },
          {
            case Yes => Redirect(routes.ConfirmMobileNumberController.updateMobileNumber())
              .addingToSession(prepopulationMobileKey -> "")
            case No => Redirect(appConfig.manageVatSubscriptionServicePath)
          }
        )
      case None =>
        Redirect(routes.CaptureMobileNumberController.show())
    }
  }
}
