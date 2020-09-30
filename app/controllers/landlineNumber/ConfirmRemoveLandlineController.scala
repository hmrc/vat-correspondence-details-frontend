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

package controllers.landlineNumber

import common.SessionKeys._
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent}
import views.html.landlineNumber.ConfirmRemoveLandlineView
import forms.RemovalForm._

import scala.concurrent.Future

class ConfirmRemoveLandlineController @Inject()(val confirmRemoveLandline: ConfirmRemoveLandlineView,
                                                errorHandler: ErrorHandler)
                                               (implicit appConfig: AppConfig,
                                                authComps: AuthPredicateComponents,
                                                inFlightComps: InFlightPredicateComponents) extends BaseController {

  def show(): Action[AnyContent] = (allowAgentPredicate andThen inFlightLandlineNumberPredicate).async { implicit user =>
    user.session.get(validationLandlineKey) match {
      case Some(landline) =>
        Future.successful(Ok(confirmRemoveLandline(landline)))
      case None =>
        Future.successful(Redirect(routes.CaptureLandlineNumberController.show()))
    }
  }

  def removeLandlineNumber(): Action[AnyContent] = (allowAgentPredicate andThen inFlightLandlineNumberPredicate) {
    implicit user =>
    user.session.get(validationLandlineKey) match {
      case Some(_) =>
        removalForm.bindFromRequest.fold(
          formWithErrors => {
            errorHandler.showBadRequestError
          },

          formSuccess => {
            Redirect(controllers.landlineNumber.routes.ConfirmLandlineNumberController.updateLandlineNumber())
              .addingToSession(prepopulationLandlineKey -> "")
          }
        )
      case None =>
        Redirect(routes.CaptureLandlineNumberController.show())

    }
  }
}
