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

package controllers.mobileNumber

import javax.inject.{Inject, Singleton}
import audit.AuditingService
import audit.models.ChangedMobileNumberAuditModel
import common.SessionKeys
import common.SessionKeys._
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.YesNoForm
import models.{No, User, Yes, YesNo}
import models.customerInformation.UpdatePPOBSuccess
import models.errors.ErrorModel
import models.viewModels.CheckYourAnswersViewModel
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.VatSubscriptionService
import utils.LoggerUtil
import views.html.mobileNumber.ConfirmRemoveMobileView
import views.html.templates.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmMobileNumberController @Inject()(errorHandler: ErrorHandler,
                                              vatSubscriptionService: VatSubscriptionService,
                                              confirmMobileNumberView: CheckYourAnswersView,
                                              confirmRemoveMobile: ConfirmRemoveMobileView,
                                              auditService: AuditingService)
                                             (implicit appConfig: AppConfig,
                                              mcc: MessagesControllerComponents,
                                              authComps: AuthPredicateComponents,
                                              inFlightComps: InFlightPredicateComponents) extends BaseController with LoggerUtil {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (allowAgentPredicate andThen inFlightMobileNumberPredicate) { implicit user =>
    val prepopulationMobile = user.session.get(prepopulationMobileKey).filter(_.nonEmpty)

    prepopulationMobile match {
      case None => Redirect(routes.CaptureMobileNumberController.show)
      case Some(prepopMobile) => Ok(
        confirmMobileNumberView(CheckYourAnswersViewModel(
          question = "checkYourAnswers.mobileNumber",
          answer = prepopMobile,
          changeLink = routes.CaptureMobileNumberController.show.url,
          changeLinkHiddenText = "checkYourAnswers.mobileNumber.edit",
          continueLink = routes.ConfirmMobileNumberController.updateMobileNumber
        ))
      )
    }
  }

  private[controllers] def performUpdate(newNumber: String, pageUrl: String)(implicit user: User[_]): Future[Result] =
    vatSubscriptionService.updateMobileNumber(user.vrn, newNumber).map {
      case Right(UpdatePPOBSuccess(_)) =>
        auditService.extendedAudit(
          ChangedMobileNumberAuditModel(
            user.session.get(validationMobileKey).filter(_.nonEmpty),
            newNumber,
            user.vrn,
            user.isAgent,
            user.arn
          ),
          pageUrl
        )
        Redirect(controllers.routes.ChangeSuccessController.mobileNumber)
          .removingFromSession(validationMobileKey, prepopulationMobileKey)
          .addingToSession(mobileChangeSuccessful -> "true", inFlightContactDetailsChangeKey -> "true")

      case Left(ErrorModel(CONFLICT, _)) =>
        logger.warn("[ConfirmMobileNumberController][performUpdate] - There is a contact details update request " +
          "already in progress. Redirecting user to manage-vat overview page.")
        Redirect(appConfig.manageVatSubscriptionServicePath)
          .addingToSession(inFlightContactDetailsChangeKey -> "true")

      case Left(_) =>
        errorHandler.showInternalServerError
    }

  def updateMobileNumber: Action[AnyContent] = (allowAgentPredicate andThen inFlightMobileNumberPredicate).async {
    implicit user =>
      val enteredMobile = user.session.get(SessionKeys.prepopulationMobileKey)

      enteredMobile match {
        case None =>
          logger.info("[ConfirmMobileNumberController][updateMobileNumber] - No mobile number found in session")
          Future.successful(Redirect(routes.CaptureMobileNumberController.show))

        case Some(mobile) =>
          performUpdate(mobile, controllers.mobileNumber.routes.ConfirmMobileNumberController.updateMobileNumber.url)
      }
  }

  val yesNoForm: Form[YesNo] = YesNoForm.yesNoForm("confirmRemoveMobile.error")

  def removeShow: Action[AnyContent] = (allowAgentPredicate andThen inFlightMobileNumberPredicate) { implicit user =>
    user.session.get(validationMobileKey).filter(_.nonEmpty) match {
      case Some(_) =>
        Ok(confirmRemoveMobile(yesNoForm))
      case None =>
        Redirect(routes.CaptureMobileNumberController.show)
    }
  }

  def removeMobileNumber: Action[AnyContent] = (allowAgentPredicate andThen
                                                  inFlightMobileNumberPredicate).async { implicit user =>
    user.session.get(validationMobileKey).filter(_.nonEmpty) match {
      case Some(_) =>
        yesNoForm.bindFromRequest().fold(
          errorForm => {
            Future.successful(BadRequest(confirmRemoveMobile(errorForm)))
          },
          {
            case Yes =>
              performUpdate("", controllers.mobileNumber.routes.ConfirmMobileNumberController.removeMobileNumber.url)
            case No => Future.successful(Redirect(appConfig.manageVatSubscriptionServicePath))
          }
        )
      case None =>
        Future.successful(Redirect(routes.CaptureMobileNumberController.show))
    }
  }
}
