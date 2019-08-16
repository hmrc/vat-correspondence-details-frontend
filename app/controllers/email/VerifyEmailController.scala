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

import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthPredicateComponents
import controllers.BaseController
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.{Inject, Singleton}
import models.User
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.EmailVerificationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import utils.LoggerUtil.logWarn
import views.html.email.VerifyEmailView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifyEmailController @Inject()(val emailVerificationService: EmailVerificationService,
                                      val errorHandler: ErrorHandler,
                                      verifyEmailView: VerifyEmailView)
                                     (implicit val appConfig: AppConfig,
                                      mcc: MessagesControllerComponents,
                                      authComps: AuthPredicateComponents,
                                      inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (blockAgentPredicate andThen inFlightEmailPredicate).async { implicit user =>

    extractSessionEmail(user) match {
      case Some(email) => Future.successful(Ok(verifyEmailView(email)))
      case _ => Future.successful(Redirect(routes.CaptureEmailController.show()))
    }
  }

  def sendVerification: Action[AnyContent] = blockAgentPredicate.async { implicit user =>

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(user.headers, Some(user.session))

    extractSessionEmail(user) match {
      case Some(email) =>
        emailVerificationService.createEmailVerificationRequest(email, routes.ConfirmEmailController.updateEmailAddress().url).map{
          case Some(true) => Redirect(routes.VerifyEmailController.show())
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

  private[controllers] def extractSessionEmail(user: User[AnyContent]): Option[String] = {
    user.session.get(SessionKeys.prepopulationEmailKey).filter(_.nonEmpty).orElse(None)
  }
}
