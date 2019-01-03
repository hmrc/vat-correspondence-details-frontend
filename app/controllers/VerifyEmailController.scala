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

import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, InflightPPOBPredicate}
import javax.inject.{Inject, Singleton}

import models.User
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.EmailVerificationService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifyEmailController @Inject()(val authenticate: AuthPredicate,
                                      val inflightCheck: InflightPPOBPredicate,
                                      val messagesApi: MessagesApi,
                                      val emailVerificationService: EmailVerificationService,
                                      val errorHandler: ErrorHandler,
                                      implicit val appConfig: AppConfig,
                                      implicit val ec: ExecutionContext) extends BaseController {

  def show: Action[AnyContent] = (authenticate andThen inflightCheck).async { implicit user =>

    extractSessionEmail(user) match {
      case Some(email) => Future.successful(Ok(views.html.verify_email(email)))
      case _ => Future.successful(Redirect(routes.CaptureEmailController.show()))
    }
  }

  def sendVerification: Action[AnyContent] = authenticate.async { implicit user =>

    extractSessionEmail(user) match {
      case Some(email) =>
        emailVerificationService.createEmailVerificationRequest(email, routes.ConfirmEmailController.updateEmailAddress().url).map{
          case Some(true) => Redirect(routes.VerifyEmailController.show())
          case Some(false) =>
            Logger.warn(
              "[VerifyEmailController][sendVerification] - " +
                "Unable to send email verification request. Service responded with 'already verified'"
            )
            Redirect(routes.ConfirmEmailController.updateEmailAddress())
          case _ =>  errorHandler.showInternalServerError
        }

      case _ => Future.successful(Redirect(routes.CaptureEmailController.show()))
    }

  }

  private[controllers] def extractSessionEmail(user: User[AnyContent]): Option[String] = {
    user.session.get(SessionKeys.emailKey).filter(_.nonEmpty).orElse(None)
  }
}
