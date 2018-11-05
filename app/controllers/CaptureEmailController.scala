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

import audit.AuditingService
import audit.models.AttemptedEmailAddressAuditModel
import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.predicates.{AuthPredicate, InflightPPOBPredicate}
import forms.EmailForm._
import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.VatSubscriptionService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

@Singleton
class CaptureEmailController @Inject()(val authenticate: AuthPredicate,
                                       val inflightCheck: InflightPPOBPredicate,
                                       val messagesApi: MessagesApi,
                                       val vatSubscriptionService: VatSubscriptionService,
                                       val errorHandler: ErrorHandler,
                                       val auditService: AuditingService,
                                       implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def show: Action[AnyContent] = (authenticate andThen inflightCheck).async { implicit user =>
    val validationEmail: Future[Option[String]] = user.session.get(SessionKeys.validationEmailKey) match {
      case Some(email) => Future.successful(Some(email))
      case _ =>
        vatSubscriptionService.getCustomerInfo(user.vrn) map {
          case Right(details) => Some(details.ppob.contactDetails.flatMap(_.emailAddress).getOrElse(""))
          case _ => None
        }
    }

    val prepopulationEmail: Future[String] = validationEmail map { validation =>
      user.session.get(SessionKeys.emailKey)
        .getOrElse(validation.getOrElse(""))
    }

    for {
      validation    <- validationEmail
      prepopulation <- prepopulationEmail
    } yield {
      validation match {
        case Some(valEmail) => Ok(views.html.capture_email(emailForm(valEmail).fill(prepopulation), emailNotChangedError = false))
          .addingToSession(SessionKeys.validationEmailKey -> valEmail)
        case _ => errorHandler.showInternalServerError
      }
    }
  }

  def submit: Action[AnyContent] = (authenticate andThen inflightCheck).async { implicit user =>
    val validationEmail: Option[String] = user.session.get(SessionKeys.validationEmailKey)
    val prepopulationEmail: Option[String] = user.session.get(SessionKeys.emailKey)

    (validationEmail, prepopulationEmail) match {
      case (Some(validation), prepopulation) => emailForm(validation).bindFromRequest.fold(
        errorForm => {
          val notChanged: Boolean = errorForm.errors.head.message == messagesApi.apply("captureEmail.error.notChanged")
          Future.successful(BadRequest(views.html.capture_email(errorForm.fill(prepopulation.getOrElse(validation)), notChanged)))
        },
        email     => {
          auditService.extendedAudit(
            AttemptedEmailAddressAuditModel(
              prepopulation,
              validation,
              user.vrn,
              user.isAgent,
              user.arn
            )
          )
          Future.successful(Redirect(controllers.routes.ConfirmEmailController.show())
            .addingToSession(SessionKeys.emailKey -> email))
        }
      )
      case (None, _) => Future.successful(errorHandler.showInternalServerError)
    }
  }
}
