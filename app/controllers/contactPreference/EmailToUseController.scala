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

package controllers.contactPreference

import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.YesNoForm
import javax.inject.{Inject, Singleton}
import models.{No, Yes, YesNo}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.VatSubscriptionService
import views.html.contactPreference.EmailToUseView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailToUseController @Inject()(val vatSubscriptionService: VatSubscriptionService,
                                     val errorHandler: ErrorHandler,
                                     emailToUseView: EmailToUseView)
                                    (implicit val appConfig: AppConfig,
                                     authComps: AuthPredicateComponents,
                                     inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = authComps.mcc.executionContext
  val form: Form[YesNo] = YesNoForm.yesNoForm("emailToUse.error")

  def show: Action[AnyContent] = (contactPreferencePredicate andThen paperPrefPredicate).async { implicit user =>
    if (appConfig.features.letterToConfirmedEmailEnabled()) {
      user.session.get(SessionKeys.contactPrefUpdate) match {
        case Some("true") =>
          lazy val validationEmail: Future[Option[String]] = user.session.get(SessionKeys.validationEmailKey) match {
            case Some(email) => Future.successful(Some(email))
            case _ =>
              vatSubscriptionService.getCustomerInfo(user.vrn) map {
                case Right(details) => Some(details.ppob.contactDetails.flatMap(_.emailAddress).getOrElse(""))
                case _ => None
              }
          }

          validationEmail map {
            case Some(email) => Ok(emailToUseView(form, email))
              .addingToSession(SessionKeys.validationEmailKey -> email)
              .addingToSession(SessionKeys.prepopulationEmailKey -> email)
              .removingFromSession(SessionKeys.contactPrefConfirmed)
            case _ => errorHandler.showInternalServerError
          }

        case _ => Future.successful(Redirect(controllers.contactPreference.routes.EmailPreferenceController.show()))
      }
    } else {
      Future.successful(NotFound(errorHandler.notFoundTemplate))
    }

  }


  def submit: Action[AnyContent] = (contactPreferencePredicate andThen paperPrefPredicate).async { implicit user =>
    if (appConfig.features.letterToConfirmedEmailEnabled()) {

      user.session.get(SessionKeys.contactPrefUpdate) match {
        case Some("true") =>
          lazy val validationEmail: Option[String] = user.session.get(SessionKeys.validationEmailKey)

          validationEmail match {
            case Some(email) => form.bindFromRequest().fold(
              error =>
                Future.successful(BadRequest(emailToUseView(error, email))),
              {
                case Yes =>
                  //TODO Add call to vat-subscription once the appropriate call exist
                  Future.successful(Redirect(controllers.contactPreference.routes.ContactPreferenceConfirmationController.show("email"))
                  .addingToSession( SessionKeys.letterToEmailChangeSuccessful -> "true",
                                    SessionKeys.contactPrefConfirmed -> "true"))
                case No => Future.successful(Redirect(controllers.email.routes.CaptureEmailController.show()))
              }
            )
            case _ => Future.successful(errorHandler.showInternalServerError)
          }
        case _ => Future.successful(Redirect(controllers.contactPreference.routes.EmailPreferenceController.show()))
      }
    } else {
      Future.successful(NotFound(errorHandler.notFoundTemplate))
    }
  }
}
