/*
 * Copyright 2023 HM Revenue & Customs
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
import audit.models.{AttemptedContactPrefEmailAuditModel, AttemptedEmailAddressAuditModel}
import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.EmailForm._
import javax.inject.{Inject, Singleton}
import models.User
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

  private def sessionValidationEmail(implicit user: User[AnyContent]): Option[String] = user.session.get(SessionKeys.validationEmailKey)
  private def sessionPrePopulationEmail(implicit user: User[AnyContent]): Option[String] = user.session.get(SessionKeys.prepopulationEmailKey)

  private def prePopulationEmail(currentEmail: String)(implicit user: User[AnyContent]): String =
    sessionPrePopulationEmail.getOrElse(currentEmail)

  def show: Action[AnyContent] = (blockAgentPredicate andThen inFlightEmailPredicate) { implicit user =>
    sessionValidationEmail match {
        case Some(valEmail) =>
          Ok(captureEmailView(
            emailForm(valEmail).fill(prePopulationEmail(valEmail)),
            emailNotChangedError = false,
            valEmail,
            controllers.email.routes.CaptureEmailController.submit,
            letterToConfirmedEmail = false
          ))
        case _ => errorHandler.showInternalServerError
      }
  }

  def submit: Action[AnyContent] = (blockAgentPredicate andThen inFlightEmailPredicate).async { implicit user =>
    (sessionValidationEmail, sessionPrePopulationEmail) match {
      case (Some(validation), _) => emailForm(validation).bindFromRequest().fold(
        errorForm => {
          val notChanged: Boolean = errorForm.errors.head.message == user.messages.apply("captureEmail.error.notChanged")
          Future.successful(BadRequest(captureEmailView(
            errorForm,
            notChanged,
            validation,
            controllers.email.routes.CaptureEmailController.submit,
            letterToConfirmedEmail = false
          )))
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
            controllers.email.routes.CaptureEmailController.submit.url
          )
          Future.successful(Redirect(routes.ConfirmEmailController.show)
            .addingToSession(SessionKeys.prepopulationEmailKey -> email))
        }
      )
      case (None, _) => Future.successful(errorHandler.showInternalServerError)
    }
  }

  def showPrefJourney: Action[AnyContent] = (
    contactPreferencePredicate andThen
      paperPrefPredicate andThen
      inFlightContactPrefPredicate) { implicit user =>
        sessionValidationEmail match {
          case Some(valEmail) =>
            Ok(captureEmailView(
              emailForm(valEmail).fill(prePopulationEmail(valEmail)),
              emailNotChangedError = false,
              valEmail,
              controllers.email.routes.CaptureEmailController.submitPrefJourney,
              letterToConfirmedEmail = true
            ))
          case _ => errorHandler.showInternalServerError
        }
      }

  def submitPrefJourney: Action[AnyContent] = (
    contactPreferencePredicate andThen
      paperPrefPredicate andThen
      inFlightContactPrefPredicate).async { implicit user =>

      (sessionValidationEmail, sessionPrePopulationEmail) match {
        case (Some(validation), _) => emailForm(validation).bindFromRequest().fold(
          errorForm => {
            val notChanged: Boolean = errorForm.errors.head.message == user.messages.apply("captureEmail.error.notChanged")
            Future.successful(BadRequest(captureEmailView(
              errorForm,
              notChanged,
              validation,
              controllers.email.routes.CaptureEmailController.submitPrefJourney,
              letterToConfirmedEmail = true
            )))
          },
          email     => {
            auditService.extendedAudit(
              AttemptedContactPrefEmailAuditModel(
                Option(validation).filter(_.nonEmpty),
                email,
                user.vrn
              ),
              controllers.email.routes.CaptureEmailController.submitPrefJourney.url
            )
            Future.successful(Redirect(controllers.email.routes.ConfirmEmailController.showContactPref)
              .addingToSession(SessionKeys.prepopulationEmailKey -> email))
          }
        )
        case (None, _) => Future.successful(errorHandler.showInternalServerError)
      }
    }
}
