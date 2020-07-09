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
import play.api.mvc._
import views.html.contactPreference.PreferenceConfirmationView

import scala.concurrent.Future

@Singleton
class ContactPreferenceConfirmationController @Inject()(preferenceConfirmationView: PreferenceConfirmationView)
                                                       (implicit val appConfig: AppConfig,
                                                        authComps: AuthPredicateComponents,
                                                        inFlightComps: InFlightPredicateComponents) extends BaseController {

  def show(changeType: String): Action[AnyContent] =
    (contactPreferencePredicate andThen paperPrefPredicate).async { implicit user =>
      if (appConfig.features.letterToConfirmedEmailEnabled()) {
        changeType match {
          case "email" => sessionGuard(letterToEmailChangeSuccessful, validationEmailKey)
          case "letter" => sessionGuard(emailToLetterChangeSuccessful, validationPPOBKey)
        }
      } else {
        Future.successful(NotFound(authComps.errorHandler.notFoundTemplate))
      }
    }

  private[controllers] def sessionGuard(changeKey: String, validationKey: String)(implicit user: User[_]): Future[Result] =
    user.session.get(validationKey) match {
      case Some(validationValue) if user.session.get(changeKey).exists(_.equals("true")) =>
        renderView(changeKey, validationValue)
      case _ =>
        val redirectLocation: Call = changeKey match {
          case `letterToEmailChangeSuccessful` => controllers.contactPreference.routes.EmailToUseController.show()
          case `emailToLetterChangeSuccessful` => controllers.contactPreference.routes.EmailToUseController.show() //TODO Change this to the correct page
        }
        Future.successful(Redirect(redirectLocation))
    }

  private[controllers] def renderView(changeKey: String, validationValue: String)(implicit user: User[_]): Future[Result] =
    Future.successful(Ok(preferenceConfirmationView(validationValue, changeKey)))
}
