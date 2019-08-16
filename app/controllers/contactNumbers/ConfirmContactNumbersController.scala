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
import views.html.contactNumbers.ConfirmContactNumbersView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmContactNumbersController @Inject()(val errorHandler: ErrorHandler,
                                                val vatSubscriptionService: VatSubscriptionService,
                                                confirmContactNumbersView: ConfirmContactNumbersView)
                                               (implicit val appConfig: AppConfig,
                                                mcc: MessagesControllerComponents,
                                                authComps: AuthPredicateComponents,
                                                inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (allowAgentPredicate andThen inFlightContactNumbersPredicate) { implicit user =>
    val prepopulationLandline = user.session.get(prepopulationLandlineKey).filter(_.nonEmpty)
    val validationLandline = user.session.get(validationLandlineKey).filter(_.nonEmpty)
    val prepopulationMobile = user.session.get(prepopulationMobileKey).filter(_.nonEmpty)
    val validationMobile = user.session.get(validationMobileKey).filter(_.nonEmpty)

    def numberToShow(prepopulatedValue : Option[String], validationValue : Option[String]) : String = {
      (prepopulatedValue, validationValue) match {
        case (None, None) => request2Messages(user)("confirmPhoneNumbers.notProvided")
        case (None, _) => request2Messages(user)("confirmPhoneNumbers.numberRemoved")
        case (Some(prepop), _) => prepop
      }
    }

    (prepopulationLandline, prepopulationMobile) match {
      case (None, None) => Redirect(controllers.contactNumbers.routes.CaptureContactNumbersController.show())
      case _ => Ok(confirmContactNumbersView(
        numberToShow(prepopulationLandline, validationLandline),
        numberToShow(prepopulationMobile, validationMobile))
      )
    }
  }

  def updateContactNumbers: Action[AnyContent] = (allowAgentPredicate andThen inFlightContactNumbersPredicate).async {
    implicit user =>
      val enteredLandline = user.session.get(SessionKeys.prepopulationLandlineKey).filter(_.nonEmpty)
      val enteredMobile = user.session.get(SessionKeys.prepopulationMobileKey).filter(_.nonEmpty)
      (enteredLandline, enteredMobile) match {
        case (None, None) =>
          logInfo("[ConfirmPhoneNumbersController][updateEmailAddress] - No phone numbers found in session")
          Future.successful(Redirect(routes.ConfirmContactNumbersController.show()))

        case (landline, mobile) => vatSubscriptionService.updateContactNumbers(user.vrn, landline, mobile).map {
          case Right(UpdatePPOBSuccess(_)) =>
            Redirect(routes.ContactNumbersChangeSuccessController.show())
              .removingFromSession(prepopulationLandlineKey, prepopulationMobileKey, validationMobileKey, validationLandlineKey)
              .addingToSession(phoneNumberChangeSuccessful -> "true", inFlightContactDetailsChangeKey -> "true")

          case Left(ErrorModel(CONFLICT, _)) =>
            logWarn("[ConfirmPhoneNumbersController][updatePhoneNumbers] - There is a contact details update request " +
              "already in progress. Redirecting user to manage-vat overview page.")
            Redirect(appConfig.manageVatSubscriptionServicePath)
              .addingToSession(inFlightContactDetailsChangeKey -> "true")

          case Left(_) =>
            errorHandler.showInternalServerError
        }
      }
  }
}
