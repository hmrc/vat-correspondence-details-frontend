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

package controllers.website

import common.SessionKeys.{validationWebsiteKey, prepopulationWebsiteKey}
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.{AuthPredicateComponents, InFlightPPOBPredicate}
import javax.inject.{Inject, Singleton}
import models.User
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VatSubscriptionService
import views.html.website.ConfirmRemoveWebsiteView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmRemoveWebsiteController @Inject()(val authComps: AuthPredicateComponents,
                                               override val mcc: MessagesControllerComponents,
                                               val errorHandler: ErrorHandler,
                                               val vatSubscriptionService: VatSubscriptionService,
                                               confirmRemoveWebsite: ConfirmRemoveWebsiteView,
                                               implicit val appConfig: AppConfig) extends BaseController(mcc, authComps) {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = allowAgentPredicate.async { implicit user =>

    extractSessionWebsiteAddress(user) match {
      case Some(website) =>
        Future.successful(Ok(confirmRemoveWebsite(website)))
      case _ =>
        Future.successful(Redirect(routes.CaptureWebsiteController.show()))
      }
  }

  def removeWebsiteAddress(): Action[AnyContent] = allowAgentPredicate.async { implicit user =>

    extractSessionWebsiteAddress(user) match {
      case Some(_) =>
        Future.successful(Redirect(routes.ConfirmWebsiteController.updateWebsite())
          .addingToSession(prepopulationWebsiteKey -> ""))
      case _ =>
        Future.successful(Redirect(routes.CaptureWebsiteController.show()))
    }
  }

  private[controllers] def extractSessionWebsiteAddress(user: User[AnyContent]): Option[String] =
    user.session.get(validationWebsiteKey).filter(_.nonEmpty)
}
