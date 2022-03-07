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

package controllers.landlineNumber

import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.BouncedEmailForm
import forms.LandlineNumberForm._

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import services.VatSubscriptionService
import views.html.email.BouncedEmailView
import views.html.landlineNumber.CaptureLandlineNumberView

import scala.concurrent.ExecutionContext

@Singleton
class CaptureLandlineNumberController @Inject()(val vatSubscriptionService: VatSubscriptionService,
                                                val errorHandler: ErrorHandler,
//                                                bouncedEmailView: BouncedEmailView,
                                                captureLandlineNumberView: CaptureLandlineNumberView)
                                               (implicit val appConfig: AppConfig,
                                                mcc: MessagesControllerComponents,
                                                authComps: AuthPredicateComponents,
                                                inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (allowAgentPredicate andThen inFlightLandlineNumberPredicate) { implicit user =>

    val validationLandline: Option[String] = user.session.get(SessionKeys.validationLandlineKey)

      val prepopulationLandline: String = user.session.get(SessionKeys.prepopulationLandlineKey).getOrElse(validationLandline.getOrElse(""))

      validationLandline match {
        case Some(landline) =>
          Ok(captureLandlineNumberView(landlineNumberForm(landline).fill(prepopulationLandline), landline))
        case _ => errorHandler.showInternalServerError
      }
  }

//  def show: Action[AnyContent] = (allowAgentPredicate andThen inFlightLandlineNumberPredicate) { implicit user =>
//
//    Ok(bouncedEmailView(BouncedEmailForm.bouncedEmailForm, "123@abc.com"))
//  }

  def submit: Action[AnyContent] = (allowAgentPredicate andThen inFlightLandlineNumberPredicate) { implicit user =>
    val validationLandline: Option[String] = user.session.get(SessionKeys.validationLandlineKey)

    validationLandline match {
      case Some(landline) =>
        landlineNumberForm(landline).bindFromRequest.fold(
          errorForm => {
            BadRequest(captureLandlineNumberView(errorForm, landline))
          },

          formValue => {
            Redirect(routes.ConfirmLandlineNumberController.show)
              .addingToSession(SessionKeys.prepopulationLandlineKey -> formValue)
          }
        )
      case _ => errorHandler.showInternalServerError
    }
  }
}
