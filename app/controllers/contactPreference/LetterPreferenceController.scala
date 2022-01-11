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

package controllers.contactPreference

import audit.AuditingService
import audit.models.PaperContactPreferenceAuditModel
import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.YesNoForm
import javax.inject.Inject
import models.contactPreferences.ContactPreference
import models.customerInformation.{PPOB, UpdatePPOBSuccess}
import models.errors.ErrorModel
import models.{No, User, Yes, YesNo}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Result}
import services.VatSubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import utils.LoggerUtil
import views.html.contactPreference.LetterPreferenceView

import scala.concurrent.{ExecutionContext, Future}

class LetterPreferenceController  @Inject()(view: LetterPreferenceView,
                                            vatSubscriptionService: VatSubscriptionService,
                                            val errorHandler: ErrorHandler,
                                            auditService: AuditingService)
                                           (implicit val appConfig: AppConfig,
                                            ec: ExecutionContext,
                                            authComps: AuthPredicateComponents,
                                            inFlightPredicateComponents: InFlightPredicateComponents) extends BaseController with LoggerUtil{

  val formYesNo: Form[YesNo] = YesNoForm.yesNoForm("letterPreference.error")

  def displayAddress(ppob: PPOB): String = ppob.address.line1 + ppob.address.postCode.fold("")(", " + _)

  def show: Action[AnyContent] = (contactPreferencePredicate andThen
                                  digitalPrefPredicate andThen
                                  inFlightContactPrefPredicate).async { implicit user =>
    vatSubscriptionService.getCustomerInfo(user.vrn) map {
      case Right(details) => Ok(view(formYesNo, displayAddress(details.ppob)))
      case _ => logger.warn("[LetterPreferenceController][show] Unable to retrieve current business address")
        authComps.errorHandler.showInternalServerError
    }
  }

  def submit: Action[AnyContent] = (contactPreferencePredicate andThen
                                    digitalPrefPredicate andThen
                                    inFlightContactPrefPredicate).async { implicit user =>
    formYesNo.bindFromRequest().fold(
      formWithErrors => {
        vatSubscriptionService.getCustomerInfo(user.vrn) map {
          case Right(details) => BadRequest(view(formWithErrors, displayAddress(details.ppob)))
          case _ =>
            logger.warn("[LetterPreferenceController][submit] Unable to retrieve current business address")
            authComps.errorHandler.showInternalServerError
        }
      },
      {
        case Yes => updateCommsPreference
        case No => Future.successful(Redirect(appConfig.btaAccountDetailsUrl))
      }
    )
  }

  private def updateCommsPreference(implicit hc: HeaderCarrier, user: User[_]): Future[Result] =
    vatSubscriptionService.updateContactPreference(user.vrn, ContactPreference.paper) flatMap {
      case Right(UpdatePPOBSuccess(_)) =>
        vatSubscriptionService.getCustomerInfo(user.vrn) map {
          case Right(details) =>
            auditService.extendedAudit(
              PaperContactPreferenceAuditModel(
                displayAddress(details.ppob),
                user.vrn
              ),
              controllers.landlineNumber.routes.ConfirmLandlineNumberController.updateLandlineNumber.url
            )
            Redirect(routes.ContactPreferenceConfirmationController.show("letter").url)
              .addingToSession(SessionKeys.emailToLetterChangeSuccessful -> "true")
          case _ => authComps.errorHandler.showInternalServerError
        }


      case Left(ErrorModel(CONFLICT, _)) =>
        logger.warn("[LetterPreferenceController][updateCommsPreference] - There is an update request " +
          "already in progress. Redirecting user to manage-vat overview page.")
        Future.successful(Redirect(appConfig.manageVatSubscriptionServicePath))

      case Left(_) =>
        Future.successful(errorHandler.showInternalServerError)
    }
}
