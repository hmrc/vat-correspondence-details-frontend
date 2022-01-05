/*
 * Copyright 2022 HM Revenue & Customs
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

import audit.AuditingService
import audit.models.ChangedMobileNumberAuditModel
import common.SessionKeys
import common.SessionKeys._
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.{Inject, Singleton}
import models.customerInformation.UpdatePPOBSuccess
import models.errors.ErrorModel
import models.viewModels.CheckYourAnswersViewModel
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VatSubscriptionService
import utils.LoggerUtil
import views.html.templates.CheckYourAnswersView
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmMobileNumberController @Inject()(val errorHandler: ErrorHandler,
                                              val vatSubscriptionService: VatSubscriptionService,
                                              confirmMobileNumberView: CheckYourAnswersView,
                                              auditService: AuditingService)
                                             (implicit val appConfig: AppConfig,
                                                mcc: MessagesControllerComponents,
                                                authComps: AuthPredicateComponents,
                                                inFlightComps: InFlightPredicateComponents) extends BaseController with LoggerUtil {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (allowAgentPredicate andThen inFlightMobileNumberPredicate) { implicit user =>
    val prepopulationMobile = user.session.get(prepopulationMobileKey).filter(_.nonEmpty)

    prepopulationMobile match {
      case None => Redirect(routes.CaptureMobileNumberController.show())
      case Some(prepopMobile) => Ok(
        confirmMobileNumberView(CheckYourAnswersViewModel(
          question = "checkYourAnswers.mobileNumber",
          answer = prepopMobile,
          changeLink = routes.CaptureMobileNumberController.show().url,
          changeLinkHiddenText = "checkYourAnswers.mobileNumber.edit",
          continueLink = routes.ConfirmMobileNumberController.updateMobileNumber().url
        ))
      )
    }
  }

  def updateMobileNumber(): Action[AnyContent] = (allowAgentPredicate andThen inFlightMobileNumberPredicate).async {
    implicit user =>
      val enteredMobile = user.session.get(SessionKeys.prepopulationMobileKey)
      val existingMobile = user.session.get(validationMobileKey).filter(_.nonEmpty)

      enteredMobile match {
        case None =>
          logger.info("[ConfirmMobileNumberController][updateMobileNumber] - No mobile number found in session")
          Future.successful(Redirect(routes.CaptureMobileNumberController.show()))

        case Some(mobile) => vatSubscriptionService.updateMobileNumber(user.vrn, mobile).map {
          case Right(UpdatePPOBSuccess(_)) =>
            auditService.extendedAudit(
              ChangedMobileNumberAuditModel(
                existingMobile,
                mobile,
                user.vrn,
                user.isAgent,
                user.arn
              ),
              controllers.mobileNumber.routes.ConfirmMobileNumberController.updateMobileNumber().url
            )
            Redirect(controllers.routes.ChangeSuccessController.mobileNumber())
              .removingFromSession(validationMobileKey)
              .addingToSession(mobileChangeSuccessful -> "true", inFlightContactDetailsChangeKey -> "true")

          case Left(ErrorModel(CONFLICT, _)) =>
            logger.warn("[ConfirmMobileNumberController][updateMobileNumber] - There is a contact details update request " +
              "already in progress. Redirecting user to manage-vat overview page.")
            Redirect(appConfig.manageVatSubscriptionServicePath)
              .addingToSession(inFlightContactDetailsChangeKey -> "true")

          case Left(_) =>
            errorHandler.showInternalServerError
        }
      }
  }
}
