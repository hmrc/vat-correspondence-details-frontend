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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VatSubscriptionService
import utils.LoggerUtil.{logInfo, logWarn}
import views.html.mobileNumber.ConfirmMobileNumberView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmMobileNumberController @Inject()(val errorHandler: ErrorHandler,
                                              val vatSubscriptionService: VatSubscriptionService,
                                              confirmMobileNumberView: ConfirmMobileNumberView,
                                              auditService: AuditingService)
                                             (implicit val appConfig: AppConfig,
                                                mcc: MessagesControllerComponents,
                                                authComps: AuthPredicateComponents,
                                                inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (allowAgentPredicate andThen inFlightMobileNumberPredicate) { implicit user =>
    val prepopulationMobile = user.session.get(prepopulationMobileKey).filter(_.nonEmpty)
    val validationMobile = user.session.get(validationMobileKey).filter(_.nonEmpty)

    def numberToShow(prepopulatedValue : Option[String], validationValue : Option[String]) : String = {
      (prepopulatedValue, validationValue) match {
        case (None, None) => request2Messages(user)("confirmPhoneNumbers.notProvided")
        case (None, _) => request2Messages(user)("confirmPhoneNumbers.numberRemoved")
        case (Some(prepop), _) => prepop
      }
    }

    prepopulationMobile match {
      case None => Redirect(routes.CaptureMobileNumberController.show())
      case _ => Ok(confirmMobileNumberView(
        numberToShow(prepopulationMobile, validationMobile))
      )
    }
  }

  def updateMobileNumber(): Action[AnyContent] = (allowAgentPredicate andThen inFlightMobileNumberPredicate).async {
    implicit user =>
      val enteredMobile = user.session.get(SessionKeys.prepopulationMobileKey)
      val existingMobile = user.session.get(validationMobileKey).filter(_.nonEmpty)

      enteredMobile match {
        case None =>
          logInfo("[ConfirmMobileNumberController][updateMobileNumber] - No mobile number found in session")
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
              .addingToSession(mobileChangeSuccessful -> "true", inFlightContactDetailsChangeKey -> "mobile")

          case Left(ErrorModel(CONFLICT, _)) =>
            logWarn("[ConfirmMobileNumberController][updateMobileNumber] - There is a contact details update request " +
              "already in progress. Redirecting user to manage-vat overview page.")
            Redirect(appConfig.manageVatSubscriptionServicePath)
              .addingToSession(inFlightContactDetailsChangeKey -> "mobile")

          case Left(_) =>
            errorHandler.showInternalServerError
        }
      }
  }
}
