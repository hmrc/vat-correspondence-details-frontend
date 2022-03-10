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

package controllers.email

import com.google.inject.Inject
import common.SessionKeys._
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.YesNoForm.yesNoForm
import models.{No, Yes}
import play.api.mvc.{Action, AnyContent, Request}
import play.mvc.Http.HeaderNames
import services.VatSubscriptionService
import utils.LoggerUtil

import scala.concurrent.ExecutionContext

class BouncedEmailController @Inject()(val errorHandler: ErrorHandler,
                                       val subscriptionService: VatSubscriptionService)
                                      (implicit val authComps: AuthPredicateComponents,
                                       inFlightComps: InFlightPredicateComponents,
                                       appConfig: AppConfig) extends BaseController with LoggerUtil {

  implicit val ec: ExecutionContext = authComps.mcc.executionContext

  private[controllers] def manageVatReferrerCheck(implicit request: Request[_]): Boolean = {
    val manageVatReferrerUrl = if(appConfig.manageVatSubscriptionServiceUrl.contains("localhost")) {
      appConfig.manageVatSubscriptionServiceUrl
    }  else  {
      appConfig.manageVatSubscriptionServicePath
    }
    request.session.get(manageVatRequestToFixEmail) match {
      case Some("true") => true
      case _ => request.headers.get(HeaderNames.REFERER).fold(false)(_.contains(manageVatReferrerUrl))
    }
  }

  def show: Action[AnyContent] = blockAgentPredicate.async { implicit user =>
    subscriptionService.getCustomerInfo(user.vrn) map {
      case Right(details) =>
        val email = details.ppob.contactDetails.flatMap(_.emailAddress)
        val emailVerified = details.ppob.contactDetails.flatMap(_.emailVerified)

        (email, emailVerified) match {
          case (Some(_), Some(true)) => Redirect(appConfig.vatOverviewUrl)
          case (Some(email), _) => Ok(email) //TODO: put view in here and pass in email along with form
            .addingToSession(validationEmailKey -> email, manageVatRequestToFixEmail -> manageVatReferrerCheck.toString)
          case _ => Redirect(appConfig.vatOverviewUrl)
        }
      case _ => errorHandler.showInternalServerError
    }
  }

  def submit: Action[AnyContent] = blockAgentPredicate { implicit user =>
    user.session.get(validationEmailKey) match {
      case Some(email) =>
        yesNoForm("error").bindFromRequest.fold(
          errorForm => BadRequest(""), //TODO: put bounced email view in here and replace yesNoForm with BouncedEmailForm
          {
            case Yes => Redirect(routes.VerifyPasscodeController.emailSendVerification)
              .addingToSession(prepopulationEmailKey -> email)
            case No => Redirect(routes.CaptureEmailController.show)
          }
        )
      case None => errorHandler.showInternalServerError
    }
  }
}
