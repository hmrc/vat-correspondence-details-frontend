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

import audit.AuditingService
import audit.models.ChangedEmailAddressAuditModel
import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.Inject
import models.User
import models.errors.ErrorModel
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{EmailVerificationService, VatSubscriptionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import utils.LoggerUtil.{logDebug, logWarn}
import views.html.email.VerifyEmailView

import scala.concurrent.{ExecutionContext, Future}

class EmailVerificationController @Inject()(
                                             emailVerificationService: EmailVerificationService,
                                             errorHandler: ErrorHandler,
                                             vatSubscriptionService: VatSubscriptionService,
                                             auditService: AuditingService,
                                             verifyEmailView: VerifyEmailView
                                           )(
                                             implicit authComps: AuthPredicateComponents,
                                             inFlightComps: InFlightPredicateComponents,
                                             mcc: MessagesControllerComponents,
                                             appConfig: AppConfig
                                           ) extends BaseController {

  implicit def nonFutureToFuture[T](input: T): Future[T] = Future.successful(input)

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (blockAgentPredicate andThen inFlightEmailPredicate).async { implicit user =>
    extractSessionEmail(user) match {
      case Some(email) => Future.successful(Ok(verifyEmailView(email)))
      case _ => handleNoEmail
    }
  }

  def checkVerificationStatus: Action[AnyContent] = (contactPreferencePredicate andThen paperPrefPredicate).async {
    implicit user =>

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(user.headers, Some(user.session))

      extractSessionEmail match {
        case Some(email) => emailVerificationService.isEmailVerified(email).flatMap {
          case Some(true) => Redirect(routes.EmailVerificationController.updateContactPrefEmail())
          case _ => emailVerificationService.createEmailVerificationRequest(
            email,
            routes.EmailVerificationController.updateContactPrefEmail().url
          ).map {
            case Some(true) =>
              Redirect(routes.EmailVerificationController.show())
            case Some(false) =>
              logDebug("[EmailVerificationController][checkVerificationStatus] Email has already been verified. " +
                "Redirecting to the update route.")
              Redirect(routes.EmailVerificationController.updateContactPrefEmail())
            case _ => errorHandler.showInternalServerError
          }
        }
        case _ => handleNoEmail
      }

  }

  def updateContactPrefEmail(): Action[AnyContent] = (contactPreferencePredicate andThen paperPrefPredicate).async {
    implicit user =>

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(user.headers, Some(user.session))
      extractSessionEmail match {
        case Some(email) => emailVerificationService.isEmailVerified(email).flatMap {
          case Some(true) => sendUpdateRequest(email)
          case _ =>
            logDebug("[EmailVerificationController][checkVerificationStatus] Email has not yet been verified.")
            Redirect(routes.EmailVerificationController.checkVerificationStatus())
        }
        case _ => handleNoEmail
      }
  }

  private[controllers] def sendUpdateRequest(email: String)(implicit user: User[_]): Future[Result] = {
    vatSubscriptionService.updateContactPrefEmail(user.vrn, email).map {
      case Right(_) =>
        auditService.extendedAudit(
          ChangedEmailAddressAuditModel(
            None,
            email,
            user.vrn,
            user.isAgent,
            None
          ), routes.EmailVerificationController.updateContactPrefEmail().url
        )
        Redirect(controllers.email.routes.EmailChangeSuccessController.show())
      case Left(ErrorModel(CONFLICT, _)) =>
        logDebug("[EmailVerificationController][sendUpdateRequest] - There is a contact details update request " +
          "already in progress. Redirecting user to manage-vat overview page.")
        Redirect(appConfig.btaAccountDetailsUrl)
      case Left(error) =>
        logWarn(s"[EmailVerificationController][sendUpdateRequest] - ${error.status}: ${error.message}")
        errorHandler.showInternalServerError
    }
  }

  private[controllers] def extractSessionEmail(implicit user: User[AnyContent]): Option[String] = {
    user.session.get(SessionKeys.prepopulationEmailKey).filter(_.nonEmpty).orElse(None)
  }

  private def handleNoEmail: Result = {
    Redirect("/") //TODO Redirect to "What is the email address" page (BTAT-8061)
  }

}
