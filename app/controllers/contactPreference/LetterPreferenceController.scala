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
import models.customerInformation.PPOB
import models.{No, Yes, YesNo}
import play.api.Logger
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.VatSubscriptionService
import views.html.contactPreference.LetterPreferenceView
import controllers.contactPreference._

import scala.concurrent.{ExecutionContext, Future}

class LetterPreferenceController  @Inject()(errorHandler: ErrorHandler,
                                            view: LetterPreferenceView,
                                            vatSubscriptionService: VatSubscriptionService)
                                           (implicit val appConfig: AppConfig,
                                            ec: ExecutionContext,
                                            mcc: MessagesControllerComponents,
                                            authComps: AuthPredicateComponents,
                                            inFlightPredicateComponents: InFlightPredicateComponents) extends BaseController {

  val formYesNo: Form[YesNo] = YesNoForm.yesNoForm("letterPreference.error")

  def displayAddress(ppob: PPOB): String = ppob.address.line1 + ppob.address.postCode.fold("")(", " + _)

  def show: Action[AnyContent] = (contactPreferencePredicate andThen digitalPrefPredicate).async { implicit user =>
    if(appConfig.features.letterToConfirmedEmailEnabled()) {
      vatSubscriptionService.getCustomerInfo(user.vrn) map {
        case Right(details) => Ok(view(formYesNo, displayAddress(details.ppob)))
        case _ =>
          Logger.warn("[LetterPreferenceController][show] Unable to retrieve current business address")
          errorHandler.showInternalServerError
      }
    } else {
      Future.successful(NotFound(errorHandler.notFoundTemplate))
    }
  }

  def submit: Action[AnyContent] = (contactPreferencePredicate andThen digitalPrefPredicate).async { implicit user =>
    if(appConfig.features.letterToConfirmedEmailEnabled()) {
      formYesNo.bindFromRequest().fold(
        formWithErrors => {
          vatSubscriptionService.getCustomerInfo(user.vrn) map {
            case Right(details) => BadRequest(view(formWithErrors, displayAddress(details.ppob)))
            case _ =>
              Logger.warn("[LetterPreferenceController][submit] Unable to retrieve current business address")
              errorHandler.showInternalServerError
          }
        },
        {
          case Yes => Future.successful(
            Redirect(routes.ContactPreferenceConfirmationController.show("letter").url)
              .addingToSession(SessionKeys.contactPrefUpdate -> "true")
          )
          case No => Future.successful(Redirect(appConfig.btaAccountDetailsUrl))
        }
      )
    } else {
      Future.successful(NotFound(errorHandler.notFoundTemplate))
    }
  }
}
