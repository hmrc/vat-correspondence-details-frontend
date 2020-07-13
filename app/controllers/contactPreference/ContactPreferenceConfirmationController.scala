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

import common.SessionKeys._
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.{Inject, Singleton}
import models.User
import play.api.Logger
import play.api.mvc._
import play.twirl.api.Html
import services.VatSubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.contactPreference.PreferenceConfirmationView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ContactPreferenceConfirmationController @Inject()(preferenceConfirmationView: PreferenceConfirmationView,
                                                        vatSubscriptionService: VatSubscriptionService)
                                                       (implicit val appConfig: AppConfig,
                                                        authComps: AuthPredicateComponents,
                                                        inFlightComps: InFlightPredicateComponents,
                                                        ec: ExecutionContext) extends BaseController {

  def show(changeType: String): Action[AnyContent] = contactPreferencePredicate async { implicit user =>
      if (appConfig.features.letterToConfirmedEmailEnabled()) {
        changeType match {
          case "email" => sessionGuard(letterToEmailChangeSuccessful)
          case "letter" => sessionGuard(emailToLetterChangeSuccessful)
        }
      } else {
        Future.successful(NotFound(authComps.errorHandler.notFoundTemplate))
      }
    }

  private[controllers] def renderLetterPreferenceView(implicit user: User[_]): Future[Result] = {
    vatSubscriptionService.getCustomerInfo(user.vrn) map {
      case Right(result) =>
        val address: Seq[String] = result.ppob.address.line1 +: Seq(
          result.ppob.address.line2,
          result.ppob.address.line3,
          result.ppob.address.line4,
          result.ppob.address.line5,
          result.ppob.address.postCode
        ).flatten
        Ok(preferenceConfirmationView(address, emailToLetterChangeSuccessful))
      case Left(_) =>
        Logger.warn("[ContactPreferenceConfirmationController][renderLetterPreferenceView] Unable to retrieve current business address")
        authComps.errorHandler.showInternalServerError
    }
  }

  private[controllers] def renderEmailPreferenceView(implicit user: User[_]): Result = {
    user.session.get(validationEmailKey) match {
      case Some(value) => Ok(preferenceConfirmationView(Seq(value), letterToEmailChangeSuccessful))
      case _ => Redirect(controllers.contactPreference.routes.EmailToUseController.show())
    }
  }

  private[controllers] def sessionGuard(changeKey: String)(implicit user: User[_]): Future[Result] = {
    val journeyComplete = user.session.get(changeKey).exists(_.equals("true"))
    changeKey match {
      case `letterToEmailChangeSuccessful` =>
        if(journeyComplete) Future(renderEmailPreferenceView) else Future(Redirect(routes.EmailToUseController.show()))
      case `emailToLetterChangeSuccessful` =>
        if(journeyComplete) renderLetterPreferenceView else Future(Redirect(routes.LetterPreferenceController.show()))
    }
  }
}
