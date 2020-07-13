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

import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.{Inject, Singleton}
import models.contactPreferences.ContactPreference.paper
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VatSubscriptionService

import scala.concurrent.ExecutionContext

@Singleton
class ContactPreferenceRedirectController @Inject()(errorHandler: ErrorHandler)
                                                   (implicit val appConfig: AppConfig,
                                                    mcc: MessagesControllerComponents,
                                                    authComps: AuthPredicateComponents,
                                                    inFlightComps: InFlightPredicateComponents,
                                                    vatSubscriptionService: VatSubscriptionService) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def redirect: Action[AnyContent] =
    blockAgentPredicate.async { implicit user =>
      vatSubscriptionService.getCustomerInfo(user.vrn) map {
        case Right(details) => details.commsPreference match {
          case Some(`paper`) => Redirect(controllers.contactPreference.routes.EmailPreferenceController.show())
          case Some(_) => Redirect(controllers.contactPreference.routes.LetterPreferenceController.show())
          case _ => errorHandler.showInternalServerError
        }
        case _ => errorHandler.showInternalServerError
      }
    }

}
