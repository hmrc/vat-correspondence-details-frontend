/*
 * Copyright 2018 HM Revenue & Customs
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
import config.AppConfig
import controllers.predicates.AuthPredicate
import javax.inject.{Inject, Singleton}
import models.User
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.EmailVerificationService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

@Singleton
class VerifyEmailController @Inject()(val authenticate: AuthPredicate,
                                      val messagesApi: MessagesApi,
                                      val emailVerificationService: EmailVerificationService,
                                      implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  val show: Action[AnyContent] = authenticate.async { implicit user =>

    extractSessionEmail(user) match {
      case Some(email) => Future.successful(Ok(views.html.verify_email(email)))
      case _ => Future.successful(Redirect(routes.CaptureEmailController.show()))
    }
  }

  val resendVerification: Action[AnyContent] = authenticate.async { implicit user =>

    extractSessionEmail(user) match {
      case Some(email) => {
        emailVerificationService.createEmailVerificationRequest(email, "").map{
          case Some(true) => Redirect(routes.VerifyEmailController.show())
          // already verified
          // TODO:call the code to post the email data as already found to be verified
          case Some(false) => Redirect(routes.ConfirmEmailController.show())
          case _ =>  InternalServerError
        }

      }

      case _ => Future.successful(Redirect(routes.CaptureEmailController.show()))
    }

  }

  private[controllers] def extractSessionEmail(user: User[AnyContent]): Option[String] = {
    user.session.get(SessionKeys.emailKey).filter(_.nonEmpty).orElse(None)
  }

}
