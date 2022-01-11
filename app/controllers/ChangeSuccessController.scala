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

package controllers

import common.SessionKeys._
import config.AppConfig
import connectors.httpParsers.ResponseHttpParser.HttpGetResult
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.{Inject, Singleton}
import models.User
import models.customerInformation.CustomerInformation
import models.viewModels.ChangeSuccessViewModel
import play.api.mvc._
import services.VatSubscriptionService
import views.html.templates.ChangeSuccessView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChangeSuccessController @Inject()(vatSubscriptionService: VatSubscriptionService,
                                        changeSuccessView: ChangeSuccessView)
                                       (implicit val appConfig: AppConfig,
                                        mcc: MessagesControllerComponents,
                                        authComps: AuthPredicateComponents,
                                        inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def landlineNumber: Action[AnyContent] = allowAgentPredicate.async { implicit user =>
    sessionGuard(landlineChangeSuccessful, prepopulationLandlineKey)
  }

  def mobileNumber: Action[AnyContent] = allowAgentPredicate.async { implicit user =>
    sessionGuard(mobileChangeSuccessful, prepopulationMobileKey)
  }

  def websiteAddress: Action[AnyContent] = allowAgentPredicate.async { implicit user =>
    sessionGuard(websiteChangeSuccessful, prepopulationWebsiteKey)
  }

  private[controllers] def sessionGuard(changeKey: String, prePopKey: String)(implicit user: User[_]): Future[Result] =
    user.session.get(prePopKey) match {
      case Some(_) if user.session.get(changeKey).exists(_.equals("true")) =>
        renderView(changeKey)
      case _ =>
        val redirectLocation: Call = changeKey match {
          case `landlineChangeSuccessful` => controllers.landlineNumber.routes.CaptureLandlineNumberController.show
          case `websiteChangeSuccessful` => controllers.website.routes.CaptureWebsiteController.show
        }
        Future.successful(Redirect(redirectLocation))
    }

  private[controllers] def renderView(changeKey: String)(implicit user: User[_]): Future[Result] =
    for {

      customerDetails <-
        vatSubscriptionService.getCustomerInfo(user.vrn)

    } yield {
      val viewModel =
        constructViewModel(user.session.get(mtdVatvcVerifiedAgentEmail), changeKey, customerDetails)
      Ok(changeSuccessView(viewModel))
    }

  private[controllers] def constructViewModel(agentEmail: Option[String],
                                              changeKey: String,
                                              customerDetails: HttpGetResult[CustomerInformation]): ChangeSuccessViewModel = {
    val preference: Option[String] = customerDetails.fold(_ => None, _.commsPreference)
    val entityName = customerDetails.fold(_ => None, _.entityName)
    val emailVerified = customerDetails.fold(_ => None, _.ppob.contactDetails.flatMap(_.emailVerified))
    val titleMessageKey: String = getTitleMessageKey(changeKey)
    ChangeSuccessViewModel(titleMessageKey, agentEmail, preference, entityName, emailVerified)
  }

  private[controllers] def getTitleMessageKey(changeKey: String): String =
    changeKey match {
      case `landlineChangeSuccessful` => "landlineChangeSuccess.title.change"
      case `mobileChangeSuccessful` => "mobileChangeSuccess.title.change"
      case `websiteChangeSuccessful` => "websiteChangeSuccess.title.change"
    }

  private[controllers] def getClientEntityName(implicit user: User[_]): Future[Option[String]] =
    user.session.get(mtdVatvcAgentClientName) match {
      case Some(entityName) => Future.successful(Some(entityName))
      case None => vatSubscriptionService.getCustomerInfo(user.vrn).map { result =>
        result.fold(_ => None, details => details.entityName)
      }
    }
  }