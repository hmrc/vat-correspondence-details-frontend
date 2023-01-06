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

package controllers.website

import audit.AuditingService
import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.WebsiteForm.websiteForm
import javax.inject.{Inject, Singleton}
import play.api.mvc._
import services.VatSubscriptionService
import views.html.website.CaptureWebsiteView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureWebsiteController @Inject()(val vatSubscriptionService: VatSubscriptionService,
                                         val errorHandler: ErrorHandler,
                                         val auditService: AuditingService,
                                         captureWebsiteView: CaptureWebsiteView)
                                        (implicit val appConfig: AppConfig,
                                         mcc: MessagesControllerComponents,
                                         authComps: AuthPredicateComponents,
                                         inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (allowAgentPredicate andThen inFlightWebsitePredicate) { implicit user =>
      val validationWebsite: Option[String] = user.session.get(SessionKeys.validationWebsiteKey)

      val prepopulationWebsite: String = user.session.get(SessionKeys.prepopulationWebsiteKey).getOrElse(validationWebsite.getOrElse(""))

        validationWebsite match {
          case Some(valWebsite) =>
            Ok(captureWebsiteView(websiteForm(valWebsite).fill(prepopulationWebsite), valWebsite))
          case _ => errorHandler.showInternalServerError
        }
  }

  def submit: Action[AnyContent] = (allowAgentPredicate andThen inFlightWebsitePredicate).async { implicit user =>
    val validationWebsite: Option[String] = user.session.get(SessionKeys.validationWebsiteKey)

    validationWebsite match {
      case Some(validation) => websiteForm(validation).bindFromRequest().fold(
        errorForm => {
          Future.successful(BadRequest(captureWebsiteView(errorForm, validation)))
        },
        website     => {
          Future.successful(Redirect(routes.ConfirmWebsiteController.show)
            .addingToSession(SessionKeys.prepopulationWebsiteKey -> website))
        }
      )
      case None => Future.successful(errorHandler.showInternalServerError)
    }
  }
}
