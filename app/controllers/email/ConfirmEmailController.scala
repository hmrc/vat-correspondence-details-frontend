/*
 * Copyright 2024 HM Revenue & Customs
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
import common.SessionKeys._
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthPredicateComponents
import controllers.BaseController
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.{Inject, Singleton}
import models.User
import models.customerInformation.UpdatePPOBSuccess
import models.errors.ErrorModel
import models.viewModels.CheckYourAnswersViewModel
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VatSubscriptionService
import utils.LoggerUtil
import views.html.templates.CheckYourAnswersView
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmEmailController @Inject()(val errorHandler: ErrorHandler,
                                       val auditService: AuditingService,
                                       val vatSubscriptionService: VatSubscriptionService,
                                       confirmEmailView: CheckYourAnswersView)
                                      (implicit val appConfig: AppConfig,
                                       mcc: MessagesControllerComponents,
                                       authComps: AuthPredicateComponents,
                                       inFlightComps: InFlightPredicateComponents) extends BaseController with LoggerUtil {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (blockAgentPredicate andThen inFlightEmailPredicate){ implicit user =>

    extractSessionEmail(user) match {
      case Some(email) =>
        Ok(
          confirmEmailView(
            CheckYourAnswersViewModel(
              question = "checkYourAnswers.emailAddress",
              answer = email,
              changeLink = routes.CaptureEmailController.show.url,
              changeLinkHiddenText = "checkYourAnswers.emailAddress.edit",
              continueLink = routes.VerifyPasscodeController.updateEmailAddress
            )
          )
        )
      case _ =>
        Redirect(routes.CaptureEmailController.show)
    }
  }

  def showContactPref: Action[AnyContent] = (
    contactPreferencePredicate andThen paperPrefPredicate andThen inFlightContactPrefPredicate
    ) { implicit user =>

    extractSessionEmail(user) match {
      case Some(email) =>
        Ok(
          confirmEmailView(
            CheckYourAnswersViewModel(
              question = "checkYourAnswers.emailAddress",
              answer = email,
              changeLink = controllers.email.routes.CaptureEmailController.showPrefJourney.url,
              changeLinkHiddenText = "checkYourAnswers.emailAddress.edit",
              continueLink = controllers.email.routes.VerifyPasscodeController.updateContactPrefEmail
            )
          )
        )
      case _ =>
        Redirect(routes.CaptureEmailController.show)
    }
  }

  def updateEmailAddress: Action[AnyContent] = (blockAgentPredicate andThen inFlightEmailPredicate).async { implicit user =>

    extractSessionEmail(user) match {
      case Some(email) =>
        vatSubscriptionService.updateEmail(user.vrn, email) map {
          case Right(UpdatePPOBSuccess(message)) if message.isEmpty =>
            Redirect(routes.VerifyEmailController.emailSendVerification)

          case Right(UpdatePPOBSuccess(_)) =>
            auditService.extendedAudit(
              ChangedEmailAddressAuditModel(
                user.session.get(validationEmailKey),
                email,
                user.vrn
              ),
              controllers.email.routes.EmailChangeSuccessController.show.url
            )
            Redirect(routes.EmailChangeSuccessController.show)
              .removingFromSession(prepopulationEmailKey, validationEmailKey, manageVatRequestToFixEmail)
              .addingToSession(emailChangeSuccessful -> "true", inFlightContactDetailsChangeKey -> "true")

          case Left(ErrorModel(CONFLICT, _)) =>
            logger.warn("[ConfirmEmailController][updateEmailAddress] - There is an email address update request " +
              "already in progress. Redirecting user to manage-vat overview page.")
            Redirect(appConfig.manageVatSubscriptionServicePath)
              .addingToSession(inFlightContactDetailsChangeKey -> "true")

          case Left(_) =>
            errorHandler.showInternalServerError
        }

      case _ =>
        logger.info("[ConfirmEmailController][updateEmailAddress] - No email address found in session")
        Future.successful(Redirect(routes.CaptureEmailController.show))
    }
  }

  private[controllers] def extractSessionEmail(user: User[AnyContent]): Option[String] = {
    user.session.get(prepopulationEmailKey).filter(_.nonEmpty)
  }
}
