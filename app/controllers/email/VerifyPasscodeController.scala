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

import audit.AuditingService
import audit.models.{ChangedContactPrefEmailAuditModel, ChangedEmailAddressAuditModel}
import common.SessionKeys
import common.SessionKeys.{emailChangeSuccessful, inFlightContactDetailsChangeKey, prepopulationEmailKey, validationEmailKey}
import config.{AppConfig, ErrorHandler}
import connectors.httpParsers.VerifyPasscodeHttpParser._
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.PasscodeForm
import javax.inject.{Inject, Singleton}
import models.User
import models.customerInformation.UpdatePPOBSuccess
import models.errors.ErrorModel
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{EmailVerificationService, VatSubscriptionService}
import utils.LoggerUtil
import views.html.email.{PasscodeErrorView, PasscodeView}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifyPasscodeController @Inject()(emailVerificationService: EmailVerificationService,
                                         errorHandler: ErrorHandler,
                                         passcodeView: PasscodeView,
                                         passcodeErrorView: PasscodeErrorView,
                                         vatSubscriptionService: VatSubscriptionService,
                                         auditService: AuditingService)
                                        (implicit val appConfig: AppConfig,
                                         mcc: MessagesControllerComponents,
                                         authComps: AuthPredicateComponents,
                                         inFlightComps: InFlightPredicateComponents) extends BaseController with LoggerUtil {

  implicit val ec: ExecutionContext = mcc.executionContext

  def emailShow: Action[AnyContent] = (blockAgentPredicate andThen inFlightEmailPredicate) { implicit user =>
    extractSessionEmail match {
        case Some(email) => Ok(passcodeView(email, PasscodeForm.form, contactPrefJourney = false))
        case _ => Redirect(routes.CaptureEmailController.show)
      }
    }

  def emailSubmit: Action[AnyContent] = (blockAgentPredicate andThen inFlightEmailPredicate).async { implicit user =>
      extractSessionEmail match {
        case Some(email) => PasscodeForm.form.bindFromRequest().fold(
          error => {
            Future.successful(BadRequest(passcodeView(email, error, contactPrefJourney = false)))
          },
          passcode => {
            emailVerificationService.verifyPasscode(email, passcode).map {
              case Right(SuccessfullyVerified) | Right(AlreadyVerified) =>
                Redirect(routes.VerifyPasscodeController.updateEmailAddress)
              case Right(TooManyAttempts) => BadRequest(passcodeErrorView("passcode.error.tooManyAttempts"))
              case Right(PasscodeNotFound) => BadRequest(passcodeErrorView("passcode.error.expired"))
              case Right(IncorrectPasscode) =>
                BadRequest(passcodeView(
                  email,
                  PasscodeForm.form.withError("passcode", "passcode.error.invalid"),
                  contactPrefJourney = false
                ))
              case _ => errorHandler.showInternalServerError
            }
          }
        )
        case _ => Future.successful(Redirect(routes.CaptureEmailController.show))
      }
  }

  def emailSendVerification: Action[AnyContent] = blockAgentPredicate.async { implicit user =>
      val langCookieValue = user.cookies.get("PLAY_LANG").map(_.value).getOrElse("en")
      extractSessionEmail match {
        case Some(email) => emailVerificationService.createEmailPasscodeRequest(email, langCookieValue) map {
          case Some(true) => Redirect(routes.VerifyPasscodeController.emailShow)
          case Some(false) =>
            logger.debug(
            "[VerifyPasscodeController][emailSendVerification] - " +
              "Unable to send email verification request. Service responded with 'already verified'"
            )
            Redirect(routes.VerifyPasscodeController.updateEmailAddress)
          case _ =>  errorHandler.showInternalServerError
        }
        case _ => Future.successful(Redirect(routes.CaptureEmailController.show))
      }
  }

  def updateEmailAddress(): Action[AnyContent] = (blockAgentPredicate andThen inFlightEmailPredicate).async { implicit user =>
      extractSessionEmail(user) match {
        case Some(email) =>
          vatSubscriptionService.updateEmail(user.vrn, email) map {
            case Right(UpdatePPOBSuccess(message)) if message.isEmpty =>
              Redirect(routes.VerifyPasscodeController.emailSendVerification)

            case Right(UpdatePPOBSuccess(_)) =>
              auditService.extendedAudit(
                ChangedEmailAddressAuditModel(
                  user.session.get(validationEmailKey),
                  email,
                  user.vrn,
                  user.isAgent,
                  user.arn
                ),
                controllers.email.routes.ConfirmEmailController.updateEmailAddress.url
              )
              Redirect(routes.EmailChangeSuccessController.show)
                .removingFromSession(prepopulationEmailKey, validationEmailKey)
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
          logger.info("[VerifyPasscodeController][updateEmailAddress] - No email address found in session")
          Future.successful(Redirect(routes.CaptureEmailController.show))
      }
  }

  def contactPrefShow: Action[AnyContent] = (contactPreferencePredicate andThen
                                             paperPrefPredicate andThen
                                             inFlightContactPrefPredicate) { implicit user =>
      extractSessionEmail match {
        case Some(email) => Ok(passcodeView(email, PasscodeForm.form, contactPrefJourney = true))
        case _ => Redirect(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect)
      }
  }

  def contactPrefSubmit: Action[AnyContent] = (contactPreferencePredicate andThen
                                               paperPrefPredicate andThen
                                               inFlightContactPrefPredicate).async { implicit user =>
      extractSessionEmail match {
        case Some(email) => PasscodeForm.form.bindFromRequest().fold(
          error => {
            Future.successful(BadRequest(passcodeView(email, error, contactPrefJourney = true)))
          },
          passcode => {
            emailVerificationService.verifyPasscode(email, passcode).map {
              case Right(SuccessfullyVerified) | Right(AlreadyVerified) =>
                Redirect(routes.VerifyPasscodeController.updateContactPrefEmail)
              case Right(TooManyAttempts) => BadRequest(passcodeErrorView("passcode.error.tooManyAttempts"))
              case Right(PasscodeNotFound) => BadRequest(passcodeErrorView("passcode.error.expired"))
              case Right(IncorrectPasscode) =>
                BadRequest(passcodeView(
                  email,
                  PasscodeForm.form.withError("passcode", "passcode.error.invalid"),
                  contactPrefJourney = true
                ))
              case _ => errorHandler.showInternalServerError
            }
          }
        )
        case _ =>
          Future.successful(Redirect(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect))
      }
  }

  def contactPrefSendVerification: Action[AnyContent] = (contactPreferencePredicate andThen
                                                         paperPrefPredicate andThen
                                                         inFlightContactPrefPredicate).async { implicit user =>

    val langCookieValue = user.cookies.get("PLAY_LANG").map(_.value).getOrElse("en")

     extractSessionEmail match {
        case Some(email) => emailVerificationService.createEmailPasscodeRequest(email, langCookieValue) map {
          case Some(true) => Redirect(routes.VerifyPasscodeController.contactPrefShow)
          case Some(false) =>
            logger.debug(
              "[VerifyPasscodeController][contactPrefSendVerification] - " +
                "Unable to send verification request. Service responded with 'already verified'"
            )
            Redirect(routes.VerifyPasscodeController.updateContactPrefEmail)
          case _ =>  errorHandler.showInternalServerError
        }
        case _ => Future.successful(Redirect(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect))
      }
  }


  def updateContactPrefEmail(): Action[AnyContent] = (contactPreferencePredicate andThen
                                                      paperPrefPredicate andThen
                                                      inFlightContactPrefPredicate).async { implicit user =>
      extractSessionEmail match {
        case Some(email) => emailVerificationService.isEmailVerified(email).flatMap {
          case Some(true) => sendUpdateRequest(email)
          case _ =>
            logger.debug("[VerifyPasscodeController][updateContactPrefEmail] Email has not yet been verified.")
            Future.successful(Redirect(routes.VerifyPasscodeController.contactPrefSendVerification))
        }
        case _ =>
          Future.successful(Redirect(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect))
      }
  }

  private[controllers] def sendUpdateRequest(email: String)(implicit user: User[_]): Future[Result] = {
    vatSubscriptionService.updateContactPrefEmail(user.vrn, email).map {
      case Right(_) =>
        auditService.extendedAudit(ChangedContactPrefEmailAuditModel(
          user.session.get(SessionKeys.validationEmailKey).filter(_.nonEmpty),
          email,
          user.vrn
        ), routes.VerifyPasscodeController.updateContactPrefEmail.url)
        Redirect(controllers.email.routes.EmailChangeSuccessController.show)
          .addingToSession(SessionKeys.emailChangeSuccessful -> "true", SessionKeys.inFlightContactDetailsChangeKey -> "true")
      case Left(ErrorModel(CONFLICT, _)) =>
        logger.debug("[VerifyPasscodeController][sendUpdateRequest] - There is a contact details update request " +
          "already in progress. Redirecting user to manage-vat overview page.")
        Redirect(appConfig.btaAccountDetailsUrl)
      case Left(error) =>
        logger.warn(s"[VerifyPasscodeController][sendUpdateRequest] - ${error.status}: ${error.message}")
        errorHandler.showInternalServerError
    }
  }

  def extractSessionEmail(implicit user: User[AnyContent]): Option[String] =
    user.session.get(SessionKeys.prepopulationEmailKey).filter(_.nonEmpty)
}
