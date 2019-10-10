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

package controllers.website

import common.SessionKeys.{prepopulationWebsiteKey, verifiedAgentEmail, websiteChangeSuccessful}
import config.AppConfig
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import connectors.httpParsers.ResponseHttpParser.HttpGetResult
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.Inject
import models.contactPreferences.ContactPreference
import models.errors.ErrorModel
import models.viewModels.ChangeSuccessViewModel
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ContactPreferenceService, VatSubscriptionService}
import views.html.templates.ChangeSuccessView

import scala.concurrent.{ExecutionContext, Future}

class WebsiteChangeSuccessController @Inject()(changeSuccessView: ChangeSuccessView,
                                               contactPreferenceService: ContactPreferenceService,
                                               vatSubscriptionService: VatSubscriptionService)
                                              (implicit appConfig: AppConfig,
                                               mcc: MessagesControllerComponents,
                                               authComps: AuthPredicateComponents,
                                               inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = allowAgentPredicate.async { implicit user =>
    (user.session.get(websiteChangeSuccessful), user.session.get(prepopulationWebsiteKey)) match {
      case (Some("true"), Some(website)) =>
        for {
          customerDetails <- if(user.isAgent) {vatSubscriptionService.getCustomerInfo(user.vrn)}
                             else {Future.successful(Left(ErrorModel(NO_CONTENT, "")))}
          preference <- if(user.isAgent) {Future.successful(Left(ErrorModel(NO_CONTENT, "")))}
                        else {contactPreferenceService.getContactPreference(user.vrn)}
        } yield {
          val viewModel = constructViewModel(customerDetails, preference, website, user.session.get(verifiedAgentEmail))
          Ok(changeSuccessView(viewModel)).removingFromSession(prepopulationWebsiteKey)
        }
      case _ => Future.successful(Redirect(routes.CaptureWebsiteController.show().url))
    }
  }

  private[controllers] def constructViewModel(customerInfoCall: GetCustomerInfoResponse,
                                              preferenceCall: HttpGetResult[ContactPreference],
                                              newWebsite: String,
                                              agentEmail: Option[String]): ChangeSuccessViewModel = {
    val entityName: Option[String] = customerInfoCall.fold(_ => None, details => details.entityName)
    val preference: Option[String] = preferenceCall.fold(_ => None, pref => Some(pref.preference))
    val removeWebsite: Boolean = newWebsite == ""
    val titleMessageKey = if(removeWebsite) "websiteChangeSuccess.title.remove" else "websiteChangeSuccess.title.change"
    ChangeSuccessViewModel(titleMessageKey, agentEmail, preference, entityName)
  }
}
