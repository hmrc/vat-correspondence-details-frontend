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

package controllers.predicates.contactPreference

import common.SessionKeys.currentContactPrefKey
import config.AppConfig
import javax.inject.{Inject, Singleton}
import models.contactPreferences.ContactPreference.{digital, paper}
import models.User
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.LoggerUtil
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ContactPrefPredicate @Inject()(contactPrefComps: ContactPrefPredicateComponents,
                                     blockedPref: String) extends ActionRefiner[User, User] with I18nSupport with LoggerUtil{

  implicit val appConfig: AppConfig = contactPrefComps.appConfig
  implicit val executionContext: ExecutionContext = contactPrefComps.mcc.executionContext
  implicit val messagesApi: MessagesApi = contactPrefComps.messagesApi

  override def refine[A](request: User[A]): Future[Either[Result, User[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    implicit val req: User[A] = request

    req.session.get(currentContactPrefKey) match {
      case Some(`digital`) if blockedPref == digital => Future.successful(Left(Redirect(appConfig.btaAccountDetailsUrl)))
      case Some(`paper`) if blockedPref == paper => Future.successful(Left(Redirect(appConfig.btaAccountDetailsUrl)))
      case Some(_) => Future.successful(Right(req))
      case None => getCustomerInfoCall(req.vrn)
    }
  }

  private def getCustomerInfoCall[A](vrn: String)
                                    (implicit hc: HeaderCarrier, request: User[A]): Future[Either[Result, User[A]]] =
    contactPrefComps.vatSubscriptionService.getCustomerInfo(vrn).map {
      case Right(customerInfo) => (customerInfo.commsPreference, blockedPref) match {
        case (Some(`digital`), `digital`) =>
          Left(Redirect(appConfig.btaAccountDetailsUrl).addingToSession(currentContactPrefKey -> digital))
        case (Some(`paper`), `paper`) =>
          Left(Redirect(appConfig.btaAccountDetailsUrl).addingToSession(currentContactPrefKey -> paper))
        case (Some(_), _) => Right(request)
        case (None, _) => Left(contactPrefComps.errorHandler.showInternalServerError)
      }
      case Left(error) =>
        logger.warn("[InFlightPredicate][getCustomerInfoCall] - " +
          s"The call to the GetCustomerInfo API failed. Error: ${error.message}")
        Left(contactPrefComps.errorHandler.showInternalServerError)
    }
}
