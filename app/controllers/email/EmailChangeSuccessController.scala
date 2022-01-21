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

package controllers.email

import common.SessionKeys
import config.AppConfig
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.{Inject, Singleton}
import models.viewModels.ChangeSuccessViewModel
import play.api.mvc._
import services.VatSubscriptionService
import views.html.templates.ChangeSuccessView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailChangeSuccessController @Inject()(vatSubscriptionService: VatSubscriptionService,
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
          customerDetails <- vatSubscriptionService.getCustomerInfo(user.vrn)
        } yield {

          val preference: Option[String] = customerDetails.fold(_ => None, _.commsPreference)

          val emailVerified = customerDetails.fold(_ => None, _.ppob.contactDetails.flatMap(_.emailVerified))
          val viewModel = ChangeSuccessViewModel("emailChangeSuccess.title", None, preference, None, emailVerified)
          Ok(changeSuccessView(viewModel))
        }
      case _ => Future.successful(Redirect(routes.CaptureEmailController.show.url))
    }
  }
}
