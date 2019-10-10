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

package controllers.contactNumbers

import audit.AuditingService
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
import views.html.contactNumbers.ConfirmLandlineNumberView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmLandlineNumberController @Inject()(val errorHandler: ErrorHandler,
                                                val vatSubscriptionService: VatSubscriptionService,
                                                confirmLandlineNumberView: ConfirmLandlineNumberView,
                                                auditService: AuditingService)
                                               (implicit val appConfig: AppConfig,
                                                mcc: MessagesControllerComponents,
                                                authComps: AuthPredicateComponents,
                                                inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (allowAgentPredicate andThen inFlightContactNumbersPredicate) { implicit user =>
    val prepopulationLandline = user.session.get(prepopulationLandlineKey).filter(_.nonEmpty)
    val validationLandline = user.session.get(validationLandlineKey).filter(_.nonEmpty)

    def numberToShow(prepopulatedValue : Option[String], validationValue : Option[String]) : String = {
      (prepopulatedValue, validationValue) match {
        case (None, None) => request2Messages(user)("confirmPhoneNumbers.notProvided")
        case (None, _) => request2Messages(user)("confirmPhoneNumbers.numberRemoved")
        case (Some(prepop), _) => prepop
      }
    }

    prepopulationLandline match {
      case None => Redirect(controllers.contactNumbers.routes.CaptureLandlineNumberController.show())
      case _ => Ok(confirmLandlineNumberView(
        numberToShow(prepopulationLandline, validationLandline))
      )
    }
  }

  def updateLandlineNumber: Action[AnyContent] = (allowAgentPredicate andThen inFlightContactNumbersPredicate).async {
    implicit user =>
      val enteredLandline = user.session.get(SessionKeys.prepopulationLandlineKey).filter(_.nonEmpty)

        enteredLandline match {
        case None =>
          logInfo("[ConfirmLandlineNumberController][updateLandlineNumber] - No landline number found in session")
          Future.successful(Redirect(routes.ConfirmLandlineNumberController.show()))

        case landline => vatSubscriptionService.updateContactNumbers(user.vrn, landline).map {
          case Right(UpdatePPOBSuccess(_)) =>
            Redirect(routes.ContactNumbersChangeSuccessController.show())
              .removingFromSession(prepopulationLandlineKey, validationLandlineKey)
              .addingToSession(phoneNumberChangeSuccessful -> "true", inFlightContactDetailsChangeKey -> "landline")

          case Left(ErrorModel(CONFLICT, _)) =>
            logWarn("[ConfirmLandlineNumberController][updateLandlineNumber] - There is a contact details update request " +
              "already in progress. Redirecting user to manage-vat overview page.")
            Redirect(appConfig.manageVatSubscriptionServicePath)
              .addingToSession(inFlightContactDetailsChangeKey -> "landline")

          case Left(_) =>
            errorHandler.showInternalServerError
        }
      }
  }
}
