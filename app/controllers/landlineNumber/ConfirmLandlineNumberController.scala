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

package controllers.landlineNumber

import audit.AuditingService
import audit.models.ChangedLandlineNumberAuditModel
import common.SessionKeys
import common.SessionKeys._
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.YesNoForm
import models.{No, User, Yes, YesNo}

import javax.inject.{Inject, Singleton}
import models.customerInformation.UpdatePPOBSuccess
import models.errors.ErrorModel
import models.viewModels.CheckYourAnswersViewModel
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.VatSubscriptionService
import utils.LoggerUtil
import views.html.landlineNumber.ConfirmRemoveLandlineView
import views.html.templates.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmLandlineNumberController @Inject()(errorHandler: ErrorHandler,
                                                vatSubscriptionService: VatSubscriptionService,
                                                confirmLandlineNumberView: CheckYourAnswersView,
                                                confirmRemoveLandline: ConfirmRemoveLandlineView,
                                                auditService: AuditingService)
                                               (implicit appConfig: AppConfig,
                                                mcc: MessagesControllerComponents,
                                                authComps: AuthPredicateComponents,
                                                inFlightComps: InFlightPredicateComponents) extends BaseController with LoggerUtil {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (allowAgentPredicate andThen inFlightLandlineNumberPredicate) { implicit user =>
    val prepopulationLandline = user.session.get(prepopulationLandlineKey).filter(_.nonEmpty)

    prepopulationLandline match {
      case None => Redirect(routes.CaptureLandlineNumberController.show)
      case Some(prepopLandline) => Ok(
        confirmLandlineNumberView(CheckYourAnswersViewModel(
          question = "checkYourAnswers.landlineNumber",
          answer = prepopLandline,
          changeLink = routes.CaptureLandlineNumberController.show.url,
          changeLinkHiddenText = "checkYourAnswers.landlineNumber.edit",
          continueLink = routes.ConfirmLandlineNumberController.updateLandlineNumber
        ))
      )
    }
  }

  private def performUpdate(newNumber: String, pageUrl: String)(implicit user: User[_]): Future[Result] =
    vatSubscriptionService.updateLandlineNumber(user.vrn, newNumber).map {
      case Right(UpdatePPOBSuccess(_)) =>
        auditService.extendedAudit(
          ChangedLandlineNumberAuditModel(
            user.session.get(validationLandlineKey).filter(_.nonEmpty),
            newNumber,
            user.vrn,
            user.isAgent,
            user.arn
          ),
          pageUrl
        )
        Redirect(controllers.routes.ChangeSuccessController.landlineNumber)
          .removingFromSession(validationLandlineKey, prepopulationLandlineKey)
          .addingToSession(landlineChangeSuccessful -> "true", inFlightContactDetailsChangeKey -> "true")

      case Left(ErrorModel(CONFLICT, _)) =>
        logger.warn("[ConfirmLandlineNumberController][performUpdate] - There is a contact details update request " +
          "already in progress. Redirecting user to manage-vat overview page.")
        Redirect(appConfig.manageVatSubscriptionServicePath)
          .addingToSession(inFlightContactDetailsChangeKey -> "true")

      case Left(_) =>
        errorHandler.showInternalServerError
  }

  def updateLandlineNumber: Action[AnyContent] = (allowAgentPredicate andThen inFlightLandlineNumberPredicate).async {
    implicit user =>
      val enteredLandline = user.session.get(SessionKeys.prepopulationLandlineKey)

      enteredLandline match {
        case None =>
          logger.info("[ConfirmLandlineNumberController][updateLandlineNumber] - No landline number found in session")
          Future.successful(Redirect(routes.CaptureLandlineNumberController.show))

        case Some(landline) =>
          performUpdate(landline, controllers.landlineNumber.routes.ConfirmLandlineNumberController.updateLandlineNumber.url)
      }
  }

  val yesNoForm: Form[YesNo] = YesNoForm.yesNoForm("confirmRemoveLandline.error")

  def removeShow: Action[AnyContent] = (allowAgentPredicate andThen
                                          inFlightLandlineNumberPredicate).async { implicit user =>
    user.session.get(validationLandlineKey).filter(_.nonEmpty) match {
      case Some(_) =>
        Future.successful(Ok(confirmRemoveLandline(yesNoForm)))
      case None =>
        Future.successful(Redirect(routes.CaptureLandlineNumberController.show))
    }
  }

  def removeLandlineNumber: Action[AnyContent] = (allowAgentPredicate andThen
                                                    inFlightLandlineNumberPredicate).async { implicit user =>
    user.session.get(validationLandlineKey).filter(_.nonEmpty) match {
      case Some(_) =>
        yesNoForm.bindFromRequest().fold(
          errorForm => {
            Future.successful(BadRequest(confirmRemoveLandline(errorForm)))
          },
          {
            case Yes =>
              performUpdate("", controllers.landlineNumber.routes.ConfirmLandlineNumberController.removeLandlineNumber.url)

            case No => Future.successful(Redirect(appConfig.manageVatSubscriptionServicePath))
          }
        )
      case None =>
        Future.successful(Redirect(routes.CaptureLandlineNumberController.show))

    }
  }
}
