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

import audit.AuditingService
import audit.models.ChangedWebsiteAddressAuditModel
import common.SessionKeys._
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthPredicateComponents
import controllers.BaseController
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.{Inject, Singleton}
import models.errors.ErrorModel
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VatSubscriptionService
import utils.LoggerUtil.logWarn
import views.html.website.ConfirmWebsiteView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmWebsiteController @Inject()(val errorHandler: ErrorHandler,
                                         val vatSubscriptionService: VatSubscriptionService,
                                         confirmWebsiteView: ConfirmWebsiteView,
                                         auditService: AuditingService)
                                        (implicit val appConfig: AppConfig,
                                         mcc: MessagesControllerComponents,
                                         authComps: AuthPredicateComponents,
                                         inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (allowAgentPredicate andThen inFlightWebsitePredicate) { implicit user =>

    user.session.get(prepopulationWebsiteKey).filter(_.nonEmpty) match {
      case Some(website) =>
        Ok(confirmWebsiteView(website))
      case _ =>
        Redirect(routes.CaptureWebsiteController.show())
    }
  }

  def updateWebsite(): Action[AnyContent] = (allowAgentPredicate andThen inFlightWebsitePredicate).async { implicit user =>

    user.session.get(prepopulationWebsiteKey) match {
      case Some(website) =>
        vatSubscriptionService.updateWebsite(user.vrn, website) map {
          case Right(_) =>
            auditService.extendedAudit(
              ChangedWebsiteAddressAuditModel(
                user.session.get(validationWebsiteKey),
                website,
                user.vrn,
                user.isAgent,
                user.arn
              ),
              controllers.website.routes.ConfirmWebsiteController.updateWebsite().url
            )
            Redirect(controllers.routes.ChangeSuccessController.websiteAddress())
              .addingToSession(websiteChangeSuccessful -> "true", inFlightContactDetailsChangeKey -> "website")
              .removingFromSession(validationWebsiteKey)

          case Left(ErrorModel(CONFLICT, _)) =>
            logWarn("[ConfirmWebsiteController][updateWebsite] - There is a contact details update request " +
              "already in progress. Redirecting user to manage-vat overview page.")
            Redirect(appConfig.manageVatSubscriptionServicePath)
              .addingToSession(inFlightContactDetailsChangeKey -> "website")

          case Left(_) =>
            errorHandler.showInternalServerError
        }

      case _ =>
        Future.successful(Redirect(routes.CaptureWebsiteController.show()))
    }
  }
}
