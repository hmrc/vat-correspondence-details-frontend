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

package controllers.email

import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthPredicateComponents
import controllers.BaseController
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.{Inject, Singleton}
import models.User
import models.errors.ErrorModel
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{EmailVerificationService, VatSubscriptionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import utils.LoggerUtil.{logDebug, logWarn}
import views.html.email.VerifyEmailView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifyEmailController @Inject()(val emailVerificationService: EmailVerificationService,
                                      val errorHandler: ErrorHandler,
                                      vatSubscriptionService: VatSubscriptionService,
                                      verifyEmailView: VerifyEmailView)
                                     (implicit val appConfig: AppConfig,
                                      mcc: MessagesControllerComponents,
                                      authComps: AuthPredicateComponents,
                                      inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def emailShow: Action[AnyContent] = (blockAgentPredicate andThen inFlightEmailPredicate) { implicit user =>

    extractSessionEmail match {
      case Some(email) => Ok(verifyEmailView(email, isContactPrefJourney = false))
      case _ => Redirect(routes.CaptureEmailController.show())
    }
  }

  def emailSendVerification: Action[AnyContent] = blockAgentPredicate.async { implicit user =>

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(user.headers, Some(user.session))

    extractSessionEmail match {
      case Some(email) =>
        emailVerificationService.createEmailVerificationRequest(email, routes.ConfirmEmailController.updateEmailAddress().url).map{
          case Some(true) => Redirect(routes.VerifyEmailController.emailShow())
          case Some(false) =>
            logWarn(
              "[VerifyEmailController][sendVerification] - " +
                "Unable to send email verification request. Service responded with 'already verified'"
            )
            Redirect(routes.ConfirmEmailController.updateEmailAddress())
          case _ =>  errorHandler.showInternalServerError
        }

      case _ => Future.successful(Redirect(routes.CaptureEmailController.show()))
    }

  }

  def contactPrefShow: Action[AnyContent] = (blockAgentPredicate) { implicit user =>

    if (appConfig.features.letterToConfirmedEmailEnabled()) {
      extractSessionEmail match {
        case Some(email) => Ok(verifyEmailView(email, isContactPrefJourney = true))
        case _ => Redirect(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect())
      }
    } else {
        NotFound(errorHandler.notFoundTemplate)
    }
  }


  def contactPrefSendVerification: Action[AnyContent] = blockAgentPredicate.async { implicit user =>

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(user.headers, Some(user.session))

    if (appConfig.features.letterToConfirmedEmailEnabled()) {
      extractSessionEmail match {
        case Some(email) => emailVerificationService.isEmailVerified(email).flatMap {
          case Some(true) => Future.successful(Redirect(routes.VerifyEmailController.updateContactPrefEmail()))
          case _ => emailVerificationService.createEmailVerificationRequest(
            email,
            routes.VerifyEmailController.updateContactPrefEmail().url
          ).map {
            case Some(true) =>
              Redirect(routes.VerifyEmailController.contactPrefShow())
            case Some(false) =>
              logDebug("[EmailVerificationController][checkVerificationStatus] Email has already been verified. " +
                "Redirecting to the update route.")
              Redirect(routes.VerifyEmailController.updateContactPrefEmail())
            case _ => errorHandler.showInternalServerError
          }
        }
        case _ => Future.successful(handleNoEmail)
      }
    } else {
      Future.successful(NotFound(errorHandler.notFoundTemplate))
    }
  }



  def updateContactPrefEmail(): Action[AnyContent] = (contactPreferencePredicate andThen paperPrefPredicate).async {
    implicit user =>

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(user.headers, Some(user.session))

      if (appConfig.features.letterToConfirmedEmailEnabled()) {
        extractSessionEmail match {
          case Some(email) => emailVerificationService.isEmailVerified(email).flatMap {
            case Some(true) => sendUpdateRequest(email)
            case _ =>
              logDebug("[EmailVerificationController][checkVerificationStatus] Email has not yet been verified.")
              Future.successful(Redirect(routes.VerifyEmailController.contactPrefSendVerification()))
          }
          case _ => Future.successful(handleNoEmail)
        }
      } else {
        Future.successful(NotFound(errorHandler.notFoundTemplate))
      }
  }

  private[controllers] def sendUpdateRequest(email: String)(implicit user: User[_]): Future[Result] = {
    vatSubscriptionService.updateContactPrefEmail(user.vrn, email).map {
      case Right(_) =>
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
    Redirect(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect())
  }
}
