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
import services.VatSubscriptionService
import utils.LoggerUtil
import views.html.contactPreference.EmailPreferenceView

import scala.concurrent.{ExecutionContext, Future}

class EmailPreferenceController @Inject()(vatSubscriptionService: VatSubscriptionService,
                                          errorHandler: ErrorHandler,
                                          emailPreferenceView: EmailPreferenceView)
                                         (implicit val appConfig: AppConfig,
                                          authComps: AuthPredicateComponents,
                                          executionContext: ExecutionContext,
                                          inFlightPredicateComponents: InFlightPredicateComponents) extends BaseController with LoggerUtil {

  val formYesNo: Form[YesNo] = YesNoForm.yesNoForm("emailPreference.error")

  def show: Action[AnyContent] = (contactPreferencePredicate andThen
                                  paperPrefPredicate andThen
                                  inFlightContactPrefPredicate).async { implicit user =>
    Future.successful(Ok(emailPreferenceView(formYesNo))
      .removingFromSession(SessionKeys.contactPrefUpdate)
      .addingToSession(SessionKeys.currentContactPrefKey -> paper))
  }

  def submit: Action[AnyContent] = (contactPreferencePredicate andThen
                                    paperPrefPredicate andThen
                                    inFlightContactPrefPredicate).async { implicit user =>
    formYesNo.bindFromRequest().fold (
      formWithErrors => Future.successful(BadRequest(emailPreferenceView(formWithErrors))),
      {
        case Yes =>
          vatSubscriptionService.getCustomerInfo(user.vrn).map {
            case Right(details) =>
              val result = details.ppob.contactDetails.flatMap(_.emailAddress) match {
                case Some(_) =>
                  Redirect(controllers.contactPreference.routes.EmailToUseController.show())
                case None =>
                  Redirect(controllers.contactPreference.routes.AddEmailAddressController.show())
              }
              result.addingToSession(SessionKeys.contactPrefUpdate -> "true")

            case Left(_) =>
              logger.warn("[EmailPreferenceController][.submit] Unable to retrieve email address")
              authComps.errorHandler.showInternalServerError

            }

        case No => Future.successful(Redirect(appConfig.btaAccountDetailsUrl))
      }
    )
  }
}
