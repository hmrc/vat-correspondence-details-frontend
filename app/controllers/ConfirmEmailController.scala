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

import javax.inject.{Inject, Singleton}

import audit.AuditingService
import audit.models.ChangedEmailAddressAuditModel
import common.SessionKeys.{emailKey, inFlightContactDetailsChangeKey, validationEmailKey}
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, InFlightPPOBPredicate}
import models.User
import models.customerInformation.UpdateEmailSuccess
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.VatSubscriptionService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmEmailController @Inject()(val authenticate: AuthPredicate,
                                       val inflightCheck: InFlightPPOBPredicate,
                                       val messagesApi: MessagesApi,
                                       val errorHandler: ErrorHandler,
                                       val auditService: AuditingService,
                                       val vatSubscriptionService: VatSubscriptionService,
                                       implicit val appConfig: AppConfig,
                                       implicit val ec: ExecutionContext) extends BaseController {

  def show: Action[AnyContent] = (authenticate andThen inflightCheck).async { implicit user =>

    extractSessionEmail(user) match {
      case Some(email) =>
        Future.successful(Ok(views.html.confirm_email(email)))
      case _ =>
        Future.successful(Redirect(controllers.routes.CaptureEmailController.show()))
    }
  }

  def updateEmailAddress(): Action[AnyContent] = (authenticate andThen inflightCheck).async { implicit user =>

    extractSessionEmail(user) match {
      case Some(email) =>
        vatSubscriptionService.updateEmail(user.vrn, email) map {
          case Right(UpdateEmailSuccess(message)) if message.isEmpty =>
            Redirect(routes.VerifyEmailController.sendVerification())
          case Right(UpdateEmailSuccess(_)) =>

            auditService.extendedAudit(
              ChangedEmailAddressAuditModel(
                user.session.get(validationEmailKey),
                email,
                user.vrn,
                user.isAgent,
                user.arn
              )
            )

            Redirect(routes.EmailChangeSuccessController.show()).removingFromSession(emailKey, validationEmailKey, inFlightContactDetailsChangeKey)
          case Left(_) => errorHandler.showInternalServerError
        }

      case _ =>
        Logger.info("[ConfirmEmailController][updateEmailAddress] no email address found in session")
        Future.successful(Redirect(controllers.routes.CaptureEmailController.show()))
    }
  }

  private[controllers] def extractSessionEmail(user: User[AnyContent]): Option[String] = {
    user.session.get(emailKey).filter(_.nonEmpty)
  }
}
