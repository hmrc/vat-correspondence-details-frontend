/*
 * Copyright 2019 HM Revenue & Customs
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
import config.AppConfig
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import javax.inject.{Inject, Singleton}
import play.api.mvc._
import services.ContactPreferenceService
import utils.LoggerUtil.logWarn
import views.html.email.EmailChangeSuccessView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailChangeSuccessController @Inject()(val authComps: AuthPredicateComponents,
                                             override val mcc: MessagesControllerComponents,
                                             auditService: AuditingService,
                                             contactPreferenceService: ContactPreferenceService,
                                             emailChangeSuccessView: EmailChangeSuccessView,
                                             implicit val appConfig: AppConfig) extends BaseController(mcc, authComps) {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = blockAgentPredicate.async { implicit user =>
    if (appConfig.features.contactPreferencesEnabled()) {

      contactPreferenceService.getContactPreference(user.vrn) map {
        case Right(preference) =>

          auditService.extendedAudit(
            ContactPreferenceAuditModel(
              user.vrn,
              preference.preference
            )
          )

          Ok(emailChangeSuccessView(Some(preference.preference)))

        case Left(error) =>
          logWarn("[EmailChangeSuccessController][show] Error retrieved from contactPreferenceService." +
            s" Error code: ${error.status}, Error message: ${error.message}")
          Ok(emailChangeSuccessView())

      }

    } else {
      Future.successful(Ok(emailChangeSuccessView()))
    }
  }
}
