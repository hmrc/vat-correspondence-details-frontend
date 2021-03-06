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

package controllers.email

import audit.AuditingService
import audit.models.ContactPreferenceAuditModel
import common.SessionKeys
import config.AppConfig
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.{Inject, Singleton}
import models.errors.ErrorModel
import models.viewModels.ChangeSuccessViewModel
import play.api.mvc._
import services.{ContactPreferenceService, VatSubscriptionService}
import utils.LoggerUtil.logWarn
import views.html.templates.ChangeSuccessView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailChangeSuccessController @Inject()(auditService: AuditingService,
                                             contactPreferenceService: ContactPreferenceService,
                                             vatSubscriptionService: VatSubscriptionService,
                                             changeSuccessView: ChangeSuccessView)
                                            (implicit val appConfig: AppConfig,
                                             mcc: MessagesControllerComponents,
                                             authComps: AuthPredicateComponents,
                                             inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = blockAgentPredicate.async { implicit user =>

    user.session.get(SessionKeys.emailChangeSuccessful) match {
      case Some("true") =>

        for {
          preferenceCall <- if(appConfig.features.contactPrefMigrationEnabled()){
            Future.successful(Left(ErrorModel(NO_CONTENT, "")))
          } else {
            contactPreferenceService.getContactPreference(user.vrn)
          }

          customerDetails <- vatSubscriptionService.getCustomerInfo(user.vrn)
        } yield {

          val preference: Option[String] = if (appConfig.features.contactPrefMigrationEnabled()) {
            customerDetails.fold(_ => None, _.commsPreference)
          } else {
            preferenceCall.fold(error => {
              logWarn("[EmailChangeSuccessController][show] Error retrieved from contactPreferenceService." +
                s" Error code: ${error.status}, Error message: ${error.message}")
              None
            }, pref => Some(pref.preference))
          }

          if (!appConfig.features.contactPrefMigrationEnabled() && preference.isDefined) {
            auditService.extendedAudit(
              ContactPreferenceAuditModel(user.vrn, preference.getOrElse("")),
              controllers.email.routes.EmailChangeSuccessController.show().url
            )
          }

          val emailVerified = customerDetails.fold(_ => None, _.ppob.contactDetails.flatMap(_.emailVerified))
          val viewModel = ChangeSuccessViewModel("emailChangeSuccess.title", None, preference, None, emailVerified)
          Ok(changeSuccessView(viewModel))
        }
      case _ => Future.successful(Redirect(routes.CaptureEmailController.show().url))
    }
  }
}