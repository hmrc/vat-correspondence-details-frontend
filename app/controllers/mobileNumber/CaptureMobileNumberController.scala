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

package controllers.mobileNumber

import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.MobileNumberForm._
import javax.inject.{Inject, Singleton}
import play.api.mvc._
import services.VatSubscriptionService
import views.html.mobileNumber.CaptureMobileNumberView

import scala.concurrent.ExecutionContext

@Singleton
class CaptureMobileNumberController @Inject()(val vatSubscriptionService: VatSubscriptionService,
                                              val errorHandler: ErrorHandler,
                                              captureMobileNumberView: CaptureMobileNumberView)
                                             (implicit val appConfig: AppConfig,
                                              mcc: MessagesControllerComponents,
                                              authComps: AuthPredicateComponents,
                                              inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (allowAgentPredicate andThen inFlightMobileNumberPredicate) { implicit user =>

    if(appConfig.features.changeContactDetailsEnabled()) {

      val validationMobile: Option[String] = user.session.get(SessionKeys.validationMobileKey)

      val prepopulationMobile: String = user.session.get(SessionKeys.prepopulationMobileKey).getOrElse(validationMobile.getOrElse(""))

      validationMobile match {
        case Some(mobile) =>
          Ok(captureMobileNumberView(mobileNumberForm(mobile).fill(prepopulationMobile),mobile))
        case _ => errorHandler.showInternalServerError
      }
    } else {
      NotFound(errorHandler.notFoundTemplate)
    }
  }

  def submit: Action[AnyContent] = (allowAgentPredicate andThen inFlightMobileNumberPredicate) { implicit user =>
    val validationMobile: Option[String] = user.session.get(SessionKeys.validationMobileKey)

    validationMobile match {
      case Some(mobile) =>
        mobileNumberForm(mobile).bindFromRequest.fold(
          errorForm => {
            BadRequest(captureMobileNumberView(errorForm,mobile))
          },

          formValue => {
            Redirect(routes.ConfirmMobileNumberController.show())
              .addingToSession(SessionKeys.prepopulationMobileKey -> formValue)
          }
        )
      case _ => errorHandler.showInternalServerError
    }
  }
}
