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
import connectors.httpParsers.VerifyPasscodeHttpParser._
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.PasscodeForm
import javax.inject.{Inject, Singleton}
import models.User
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.EmailVerificationService
import views.html.email.{PasscodeErrorView, PasscodeView}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifyPasscodeController @Inject()(emailVerificationService: EmailVerificationService,
                                         errorHandler: ErrorHandler,
                                         passcodeView: PasscodeView,
                                        passcodeErrorView: PasscodeErrorView)
                                        (implicit val appConfig: AppConfig,
                                         mcc: MessagesControllerComponents,
                                         authComps: AuthPredicateComponents,
                                         inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def emailShow: Action[AnyContent] = (blockAgentPredicate andThen inFlightEmailPredicate) { implicit user =>
    if (appConfig.features.emailPinVerificationEnabled()) {
      extractSessionEmail match {
        case Some(email) => Ok(passcodeView(email, PasscodeForm.form, contactPrefJourney = false))
        case _ => Redirect(routes.CaptureEmailController.show())
      }
    } else {
      NotFound(errorHandler.notFoundTemplate(user))
    }
  }

  def emailSubmit: Action[AnyContent] = (blockAgentPredicate andThen inFlightEmailPredicate).async { implicit user =>
    if (appConfig.features.emailPinVerificationEnabled()) {
      extractSessionEmail match {
        case Some(email) => PasscodeForm.form.bindFromRequest().fold(
          error => {
            Future.successful(BadRequest(passcodeView(email, error, contactPrefJourney = false)))
          },
          passcode => {
            emailVerificationService.verifyPasscode(email, passcode).map {
              case Right(SuccessfullyVerified) | Right(AlreadyVerified) =>
                Redirect(routes.VerifyPasscodeController.updateEmailAddress())
              case Right(TooManyAttempts) => BadRequest(passcodeErrorView("passcode.error.tooManyAttempts"))
              case Right(PasscodeNotFound) => BadRequest(passcodeErrorView("passcode.error.expired"))
              case _ => errorHandler.showInternalServerError
            }
          }
        )
        case _ => Future.successful(Redirect(routes.CaptureEmailController.show()))
      }
    } else {
      Future.successful(NotFound(errorHandler.notFoundTemplate(user)))
    }
  }

  def emailSendVerification: Action[AnyContent] = blockAgentPredicate { implicit user =>
    if (appConfig.features.emailPinVerificationEnabled()) {
      extractSessionEmail match {
        case Some(_) => Ok("") //TODO
        case _ => Redirect(routes.CaptureEmailController.show())
      }
    } else {
        NotFound(errorHandler.notFoundTemplate(user))
      }
    }

  def updateEmailAddress(): Action[AnyContent] = blockAgentPredicate { implicit user =>
    if (appConfig.features.emailPinVerificationEnabled()) {
      extractSessionEmail match {
        case Some(_) => Ok("") //TODO
        case _ => Redirect(routes.CaptureEmailController.show())
      }
    } else {
      NotFound(errorHandler.notFoundTemplate)
    }
  }

  def contactPrefShow: Action[AnyContent] = (contactPreferencePredicate andThen
                                             paperPrefPredicate andThen
                                             inFlightContactPrefPredicate) { implicit user =>
    if (appConfig.features.emailPinVerificationEnabled()) {
      extractSessionEmail match {
        case Some(email) => Ok(passcodeView(email, PasscodeForm.form, contactPrefJourney = true))
        case _ => Redirect(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect())
      }
    } else {
      NotFound(errorHandler.notFoundTemplate(user))
    }
  }

  def contactPrefSubmit: Action[AnyContent] = (contactPreferencePredicate andThen
                                               paperPrefPredicate andThen
                                               inFlightContactPrefPredicate).async { implicit user =>
    if (appConfig.features.emailPinVerificationEnabled()) {
      extractSessionEmail match {
        case Some(email) => PasscodeForm.form.bindFromRequest().fold(
          error => {
            Future.successful(BadRequest(passcodeView(email, error, contactPrefJourney = true)))
          },
          passcode => {
            emailVerificationService.verifyPasscode(email, passcode).map {
              case Right(SuccessfullyVerified) | Right(AlreadyVerified) =>
                Redirect(routes.VerifyPasscodeController.updateContactPrefEmail())
              case Right(TooManyAttempts) => BadRequest(passcodeErrorView("passcode.error.tooManyAttempts"))
              case Right(PasscodeNotFound) => BadRequest(passcodeErrorView("passcode.error.expired"))
              case _ => errorHandler.showInternalServerError
            }
          }
        )
        case _ =>
          Future.successful(Redirect(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect()))
      }
    } else {
      Future.successful(NotFound(errorHandler.notFoundTemplate(user)))
    }
  }

  def contactPrefSendVerification: Action[AnyContent] = (contactPreferencePredicate andThen
                                                         paperPrefPredicate) { implicit user =>

    if (appConfig.features.emailPinVerificationEnabled()) {
      extractSessionEmail match {
        case Some(_) => Ok("") //TODO
        case _ => Redirect(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect())
      }
    } else {
      NotFound(errorHandler.notFoundTemplate(user))
    }
  }

  def updateContactPrefEmail(): Action[AnyContent] = (contactPreferencePredicate andThen
                                                      paperPrefPredicate) { implicit user =>
    if (appConfig.features.emailPinVerificationEnabled()) {
      extractSessionEmail match {
        case Some(_) => Ok("") //TODO
        case _ => Redirect(controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect())
      }
    } else {
      NotFound(errorHandler.notFoundTemplate)
    }
  }

  def extractSessionEmail(implicit user: User[AnyContent]): Option[String] =
    user.session.get(SessionKeys.prepopulationEmailKey).filter(_.nonEmpty)
}
