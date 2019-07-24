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
import audit.models.ChangedEmailAddressAuditModel
import common.SessionKeys.{emailKey, inFlightContactDetailsChangeKey, validationEmailKey}
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicateComponents, InFlightPPOBPredicate}
import controllers.BaseController
import javax.inject.{Inject, Singleton}
import models.User
import models.customerInformation.UpdatePPOBSuccess
import models.errors.ErrorModel
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VatSubscriptionService
import utils.LoggerUtil.{logInfo, logWarn}
import views.html.email.ConfirmEmailView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmEmailController @Inject()(val authComps: AuthPredicateComponents,
                                       val inflightCheck: InFlightPPOBPredicate,
                                       override val mcc: MessagesControllerComponents,
                                       val errorHandler: ErrorHandler,
                                       val auditService: AuditingService,
                                       val vatSubscriptionService: VatSubscriptionService,
                                       confirmEmailView: ConfirmEmailView,
                                       implicit val appConfig: AppConfig) extends BaseController(mcc, authComps) {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (blockAgentPredicate andThen inflightCheck).async { implicit user =>

    extractSessionEmail(user) match {
      case Some(email) =>
        Future.successful(Ok(confirmEmailView(email)))
      case _ =>
        Future.successful(Redirect(routes.CaptureEmailController.show()))
    }
  }

  def updateEmailAddress(): Action[AnyContent] = (blockAgentPredicate andThen inflightCheck).async { implicit user =>

    extractSessionEmail(user) match {
      case Some(email) =>
        vatSubscriptionService.updateEmail(user.vrn, email) map {
          case Right(UpdatePPOBSuccess(message)) if message.isEmpty =>
            Redirect(routes.VerifyEmailController.sendVerification())

          case Right(UpdatePPOBSuccess(_)) =>
            auditService.extendedAudit(
              ChangedEmailAddressAuditModel(
                user.session.get(validationEmailKey),
                email,
                user.vrn,
                user.isAgent,
                user.arn
              )
            )
            Redirect(routes.EmailChangeSuccessController.show())
              .removingFromSession(emailKey, validationEmailKey, inFlightContactDetailsChangeKey)

          case Left(ErrorModel(CONFLICT, _)) =>
            logWarn("[ConfirmEmailController][updateEmailAddress] - There is an email address update request " +
              "already in progress. Redirecting user to manage-vat overview page.")
            Redirect(appConfig.manageVatSubscriptionServicePath)
              .addingToSession(inFlightContactDetailsChangeKey -> "true")

          case Left(_) =>
            errorHandler.showInternalServerError
        }

      case _ =>
        logInfo("[ConfirmEmailController][updateEmailAddress] - No email address found in session")
        Future.successful(Redirect(routes.CaptureEmailController.show()))
    }
  }

  private[controllers] def extractSessionEmail(user: User[AnyContent]): Option[String] = {
    user.session.get(emailKey).filter(_.nonEmpty)
  }
}
