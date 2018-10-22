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
import models.errors.{EmailAddressUpdateResponseModel, ErrorModel}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.VatSubscriptionService
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

@Singleton
class ConfirmEmailController @Inject()(val authenticate: AuthPredicate,
                                       val messagesApi: MessagesApi,
                                       implicit val appConfig: AppConfig,
                                       val vatSubscriptionService: VatSubscriptionService) extends FrontendController with I18nSupport {

  val show: Action[AnyContent] = authenticate.async { implicit user =>

    extractSessionEmail(user) match {
      case Some(_) =>
        Future.successful(Ok(controllers.routes.ConfirmEmailController.show().url))

      case _ =>
        Future.successful(Redirect(controllers.routes.CaptureEmailController.show().url))
    }
  }

  val updateEmailAddress: Action[AnyContent] = authenticate.async { implicit user =>

    extractSessionEmail(user) match {
      case Some(email) =>
        vatSubscriptionService.updateEmailAddress(email, user.vrn) map {
          case Right(EmailAddressUpdateResponseModel(true)) =>
            Redirect(routes.EmailChangeSuccessController.show().url)
          case Right(EmailAddressUpdateResponseModel(false)) =>
            Redirect(routes.VerifyEmailController.show().url)
          case notFound@Left(ErrorModel(NOT_FOUND, "Couldn't find a user with VRN provided")) =>
            throw new InternalServerException("updateEmail failed: status=" + notFound.left.get.message)
          case failed@Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't verify email address")) =>
            throw new InternalServerException("updateEmail failed: status=" + failed.left.get.message)
        }

      case _ =>
        Logger.info("[VatSubscriptionConnector][updateEmailAddress] no email address found in session")
        Future.successful(Redirect(controllers.routes.CaptureEmailController.show().url))
    }
  }

  private[controllers] def extractSessionEmail(user: User[AnyContent]): Option[String] = {
    user.session.get(SessionKeys.emailKey).filter(_.nonEmpty)
  }
}
