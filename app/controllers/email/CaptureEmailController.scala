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

import audit.AuditingService
import audit.models.AttemptedEmailAddressAuditModel
import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.EmailForm._
import javax.inject.{Inject, Singleton}
import play.api.mvc._
import services.VatSubscriptionService
import views.html.email.CaptureEmailView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureEmailController @Inject()(val vatSubscriptionService: VatSubscriptionService,
                                       val errorHandler: ErrorHandler,
                                       val auditService: AuditingService,
                                       captureEmailView: CaptureEmailView)
                                      (implicit val appConfig: AppConfig,
                                       mcc: MessagesControllerComponents,
                                       authComps: AuthPredicateComponents,
                                       inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  private val submitRoute: Call = controllers.email.routes.CaptureEmailController.submit()

  def show: Action[AnyContent] = (blockAgentPredicate andThen inFlightEmailPredicate).async { implicit user =>
    val validationEmail: Future[Option[String]] = user.session.get(SessionKeys.validationEmailKey) match {
      case Some(email) => Future.successful(Some(email))
      case _ =>
        vatSubscriptionService.getCustomerInfo(user.vrn) map {
          case Right(details) => Some(details.ppob.contactDetails.flatMap(_.emailAddress).getOrElse(""))
          case _ => None
        }
    }

    val prepopulationEmail: Future[String] = validationEmail map { validation =>
      user.session.get(SessionKeys.prepopulationEmailKey)
        .getOrElse(validation.getOrElse(""))
    }

    for {
      validation    <- validationEmail
      prepopulation <- prepopulationEmail
    } yield {
      validation match {
        case Some(valEmail) =>
          Ok(captureEmailView(emailForm(valEmail).fill(prepopulation), emailNotChangedError = false, valEmail, submitRoute))
          .addingToSession(SessionKeys.validationEmailKey -> valEmail)
        case _ => errorHandler.showInternalServerError
      }
    }
  }

  def submit: Action[AnyContent] = (blockAgentPredicate andThen inFlightEmailPredicate).async { implicit user =>
    val validationEmail: Option[String] = user.session.get(SessionKeys.validationEmailKey)
    val prepopulationEmail: Option[String] = user.session.get(SessionKeys.prepopulationEmailKey)

    (validationEmail, prepopulationEmail) match {
      case (Some(validation), _) => emailForm(validation).bindFromRequest.fold(
        errorForm => {
          val notChanged: Boolean = errorForm.errors.head.message == user.messages.apply("captureEmail.error.notChanged")
          Future.successful(BadRequest(captureEmailView(errorForm, notChanged, validation, submitRoute)))

        },
        email     => {
          auditService.extendedAudit(
            AttemptedEmailAddressAuditModel(
              Option(validation).filter(_.nonEmpty),
              email,
              user.vrn,
              user.isAgent,
              user.arn
            ),
            controllers.email.routes.CaptureEmailController.submit().url
          )
          Future.successful(Redirect(routes.ConfirmEmailController.show())
            .addingToSession(SessionKeys.prepopulationEmailKey -> email))
        }
      )
      case (None, _) => Future.successful(errorHandler.showInternalServerError)
    }
  }
}
