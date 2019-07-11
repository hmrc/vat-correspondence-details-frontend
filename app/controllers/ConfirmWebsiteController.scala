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

package controllers

import common.SessionKeys.{websiteKey, validationWebsiteKey, inFlightContactDetailsChangeKey}
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthPredicateComponents
import javax.inject.{Inject, Singleton}
import models.User
import models.errors.ErrorModel
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VatSubscriptionService
import utils.LoggerUtil.logWarn
import views.html.ConfirmWebsiteView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmWebsiteController @Inject()(val authComps: AuthPredicateComponents,
                                         override val mcc: MessagesControllerComponents,
                                         val errorHandler: ErrorHandler,
                                         val vatSubscriptionService: VatSubscriptionService,
                                         confirmWebsiteView: ConfirmWebsiteView,
                                         implicit val appConfig: AppConfig) extends BaseController(mcc, authComps) {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = blockAgentPredicate { implicit user =>

    extractSessionWebsite(user) match {
      case Some(website) =>
        Ok(confirmWebsiteView(website))
      case _ =>
        Redirect(controllers.routes.CaptureWebsiteController.show())
    }
  }

  def updateWebsite(): Action[AnyContent] = blockAgentPredicate.async { implicit user =>

    extractSessionWebsite(user) match {
      case Some(website) =>
        vatSubscriptionService.updateWebsite(user.vrn, website) map {
          case Right(_) =>
            Redirect(routes.ConfirmWebsiteController.show())
              .removingFromSession(websiteKey, validationWebsiteKey)

          case Left(ErrorModel(CONFLICT, _)) =>
            logWarn("[ConfirmWebsiteController][updateWebsite] - There is a contact details update request " +
              "already in progress. Redirecting user to manage-vat overview page.")
            Redirect(appConfig.manageVatSubscriptionServicePath)

          case Left(_) =>
            errorHandler.showInternalServerError
        }

      case _ =>
        Future.successful(Redirect(controllers.routes.CaptureWebsiteController.show()))
    }
  }

  private[controllers] def extractSessionWebsite(user: User[AnyContent]): Option[String] = {
    user.session.get(websiteKey).filter(_.nonEmpty)
  }
}
