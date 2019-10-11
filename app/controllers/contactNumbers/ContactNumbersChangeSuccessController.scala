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

import common.SessionKeys.{phoneNumberChangeSuccessful, verifiedAgentEmail}
import config.AppConfig
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import connectors.httpParsers.ResponseHttpParser.HttpGetResult
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.{Inject, Singleton}
import models.contactPreferences.ContactPreference
import models.errors.ErrorModel
import models.viewModels.ChangeSuccessViewModel
import play.api.mvc._
import services.{ContactPreferenceService, VatSubscriptionService}
import views.html.templates.ChangeSuccessView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ContactNumbersChangeSuccessController @Inject()(contactPreferenceService: ContactPreferenceService,
                                                      vatSubscriptionService: VatSubscriptionService,
                                                      changeSuccessView: ChangeSuccessView)
                                                     (implicit val appConfig: AppConfig,
                                                      mcc: MessagesControllerComponents,
                                                      authComps: AuthPredicateComponents,
                                                      inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = allowAgentPredicate.async { implicit user =>

    if (user.session.get(phoneNumberChangeSuccessful).exists(_.equals("true"))) {
      for {
        customerDetails <- if (user.isAgent) {vatSubscriptionService.getCustomerInfo(user.vrn)}
                           else {Future.successful(Left(ErrorModel(NO_CONTENT, "")))}
        preference <- if (user.isAgent) {Future.successful(Left(ErrorModel(NO_CONTENT, "")))}
                      else {contactPreferenceService.getContactPreference(user.vrn)}
      } yield {
        val viewModel = constructViewModel(customerDetails, preference, user.session.get(verifiedAgentEmail))
        Ok(changeSuccessView(viewModel))
      }
    } else {
      Future.successful(Redirect(routes.CaptureLandlineNumberController.show()))
    }
  }

  private[controllers] def constructViewModel(customerInfoCall: GetCustomerInfoResponse,
                                              preferenceCall: HttpGetResult[ContactPreference],
                                              agentEmail: Option[String]): ChangeSuccessViewModel = {
    val entityName: Option[String] = customerInfoCall.fold(_ => None, details => details.entityName)
    val preference: Option[String] = preferenceCall.fold(_ => None, pref => Some(pref.preference))
    val titleMessageKey: String = "contactNumbersChangeSuccess.title"
    ChangeSuccessViewModel(titleMessageKey, agentEmail, preference, entityName)
  }
}
