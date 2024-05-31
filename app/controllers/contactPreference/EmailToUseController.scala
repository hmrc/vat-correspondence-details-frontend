/*
 * Copyright 2024 HM Revenue & Customs
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

import audit.AuditingService
import audit.models.DigitalContactPreferenceAuditModel
import common.SessionKeys
import config.AppConfig
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.YesNoForm
import javax.inject.{Inject, Singleton}
import models.contactPreferences.ContactPreference
import models.customerInformation.UpdatePPOBSuccess
import models.errors.ErrorModel
import models.{No, User, Yes, YesNo}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Result}
import services.VatSubscriptionService
import utils.LoggerUtil
import views.html.contactPreference.EmailToUseView
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailToUseController @Inject()(val vatSubscriptionService: VatSubscriptionService,
                                     emailToUseView: EmailToUseView,
                                     auditService: AuditingService)
                                    (implicit val appConfig: AppConfig,
                                     authComps: AuthPredicateComponents,
                                     inFlightComps: InFlightPredicateComponents) extends BaseController with LoggerUtil {

  implicit val ec: ExecutionContext = authComps.mcc.executionContext
  val form: Form[YesNo] = YesNoForm.yesNoForm("emailToUse.error")

  def show: Action[AnyContent] = (contactPreferencePredicate andThen
                                  paperPrefPredicate andThen
                                  inFlightContactPrefPredicate).async { implicit user =>
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
          case _ => authComps.errorHandler.showInternalServerError
        }

      case _ => Future.successful(Redirect(controllers.contactPreference.routes.EmailPreferenceController.show))
    }

  }


  def submit: Action[AnyContent] = (contactPreferencePredicate andThen
                                    paperPrefPredicate andThen
                                    inFlightContactPrefPredicate).async { implicit user =>

    user.session.get(SessionKeys.contactPrefUpdate) match {
      case Some("true") =>
        lazy val validationEmail: Option[String] = user.session.get(SessionKeys.validationEmailKey)

        validationEmail match {
          case Some(email) => form.bindFromRequest().fold(
            error =>
              Future.successful(BadRequest(emailToUseView(error, email))),
            {
              case Yes => handleDynamicRouting(email)
              case No => Future.successful(Redirect(controllers.email.routes.CaptureEmailController.showPrefJourney))
            }
          )
          case _ => Future.successful(authComps.errorHandler.showInternalServerError)
        }
      case _ => Future.successful(Redirect(controllers.contactPreference.routes.EmailPreferenceController.show))
    }
  }

  private def handleDynamicRouting(email: String)(implicit user: User[_]): Future[Result] =
    vatSubscriptionService.getCustomerInfo(user.vrn).flatMap {
      case Right(details) =>
        details.ppob.contactDetails.flatMap(_.emailVerified) match {
          case Some(true) => updateCommsPreference(email)
          case _ => Future.successful(Redirect(controllers.email.routes.VerifyPasscodeController.updateContactPrefEmail))
        }
      case Left(_) => Future.successful(authComps.errorHandler.showInternalServerError)
    }

  private def updateCommsPreference(email: String)(implicit user: User[_]): Future[Result] =
    vatSubscriptionService.updateContactPreference(user.vrn, ContactPreference.digital) map {
      case Right(UpdatePPOBSuccess(_)) =>
        auditService.extendedAudit(
          DigitalContactPreferenceAuditModel(
            email,
            user.vrn
          ),
          controllers.landlineNumber.routes.ConfirmLandlineNumberController.updateLandlineNumber.url
        )
        Redirect(controllers.contactPreference.routes.ContactPreferenceConfirmationController.show("email"))
          .addingToSession(SessionKeys.letterToEmailChangeSuccessful -> "true")

      case Left(ErrorModel(CONFLICT, _)) =>
        logger.warn("[EmailToUseController][updateCommsPreference] - There is an update request " +
          "already in progress. Redirecting user to manage-vat overview page.")
        Redirect(appConfig.manageVatSubscriptionServicePath)

      case Left(_) =>
        authComps.errorHandler.showInternalServerError
    }
}
