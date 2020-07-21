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
import javax.inject.Inject
import models.contactPreferences.ContactPreference
import models.customerInformation.{PPOB, UpdatePPOBSuccess}
import models.errors.ErrorModel
import models.{No, User, Yes, YesNo}
import play.api.Logger
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.VatSubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import utils.LoggerUtil.logWarn
import views.html.contactPreference.LetterPreferenceView

import scala.concurrent.{ExecutionContext, Future}

class LetterPreferenceController  @Inject()(view: LetterPreferenceView,
                                            vatSubscriptionService: VatSubscriptionService,
                                            val errorHandler: ErrorHandler)
                                           (implicit val appConfig: AppConfig,
                                            ec: ExecutionContext,
                                            mcc: MessagesControllerComponents,
                                            authComps: AuthPredicateComponents,
                                            inFlightPredicateComponents: InFlightPredicateComponents) extends BaseController {

  val formYesNo: Form[YesNo] = YesNoForm.yesNoForm("letterPreference.error")

  def displayAddress(ppob: PPOB): String = ppob.address.line1 + ppob.address.postCode.fold("")(", " + _)

  def show: Action[AnyContent] = (contactPreferencePredicate andThen digitalPrefPredicate).async { implicit user =>
    if(appConfig.features.letterToConfirmedEmailEnabled()) {
      vatSubscriptionService.getCustomerInfo(user.vrn) map {
        case Right(details) => Ok(view(formYesNo, displayAddress(details.ppob)))
        case _ =>
          Logger.warn("[LetterPreferenceController][show] Unable to retrieve current business address")
          authComps.errorHandler.showInternalServerError
      }
    } else {
      Future.successful(NotFound(authComps.errorHandler.notFoundTemplate))
    }
  }

  def submit: Action[AnyContent] = (contactPreferencePredicate andThen digitalPrefPredicate).async { implicit user =>
    if(appConfig.features.letterToConfirmedEmailEnabled()) {
      formYesNo.bindFromRequest().fold(
        formWithErrors => {
          vatSubscriptionService.getCustomerInfo(user.vrn) map {
            case Right(details) => BadRequest(view(formWithErrors, displayAddress(details.ppob)))
            case _ =>
              Logger.warn("[LetterPreferenceController][submit] Unable to retrieve current business address")
              authComps.errorHandler.showInternalServerError
          }
        },
        {
          case Yes => updateCommsPreference(user.vrn)
          case No => Future.successful(Redirect(appConfig.btaAccountDetailsUrl))
        }
      )
    } else {
      Future.successful(NotFound(authComps.errorHandler.notFoundTemplate))
    }
  }

  private def updateCommsPreference(vrn: String)(implicit hc: HeaderCarrier, user: User[_]): Future[Result] =
    vatSubscriptionService.updateContactPreference(vrn, ContactPreference.paper) map {
      case Right(UpdatePPOBSuccess(_)) =>
        Redirect(routes.ContactPreferenceConfirmationController.show("letter").url)
          .addingToSession(SessionKeys.emailToLetterChangeSuccessful -> "true")

      case Left(ErrorModel(CONFLICT, _)) =>
        logWarn("[LetterPreferenceController][updateCommsPreference] - There is an update request " +
          "already in progress. Redirecting user to manage-vat overview page.")
        Redirect(appConfig.manageVatSubscriptionServicePath)

      case Left(_) =>
        errorHandler.showInternalServerError
    }
}
