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

package controllers.mobileNumber

import common.SessionKeys.{prepopulationMobileKey, validationMobileKey}
import config.AppConfig
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.mobileNumber.ConfirmRemoveMobileView

import scala.concurrent.Future

class ConfirmRemoveMobileController @Inject()(val confirmRemoveMobile: ConfirmRemoveMobileView)
                                             (implicit appConfig: AppConfig,
                                                mcc: MessagesControllerComponents,
                                                authComps: AuthPredicateComponents,
                                                inFlightComps: InFlightPredicateComponents) extends BaseController {

  def show(): Action[AnyContent] = (allowAgentPredicate andThen inFlightMobileNumberPredicate).async { implicit user =>
    user.session.get(validationMobileKey) match {
      case Some(mobile) =>
        Future.successful(Ok(confirmRemoveMobile(mobile)))
      case None =>
        Future.successful(Redirect(routes.CaptureMobileNumberController.show()))
    }
  }

  def removeMobileNumber(): Action[AnyContent] = (allowAgentPredicate andThen inFlightMobileNumberPredicate).async { implicit user =>
    user.session.get(validationMobileKey) match {
      case Some(_) =>
        Future.successful(Redirect(routes.ConfirmMobileNumberController.updateMobileNumber())
            .addingToSession(prepopulationMobileKey -> ""))
      case None =>
        Future.successful(Redirect(routes.CaptureMobileNumberController.show()))
    }
  }
}
