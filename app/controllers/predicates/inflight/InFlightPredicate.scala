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

package controllers.predicates.inflight

import common.SessionKeys.inFlightContactDetailsChangeKey
import config.AppConfig
import models.User
import models.customerInformation.{CustomerInformation, PendingChanges}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.{Conflict, Redirect}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import utils.LoggerUtil.{logDebug, logWarn}

import scala.concurrent.{ExecutionContext, Future}

class InFlightPredicate(inFlightComps: InFlightPredicateComponents,
                        redirectURL: String) extends ActionRefiner[User, User] with I18nSupport {

  implicit val appConfig: AppConfig = inFlightComps.appConfig
  implicit val executionContext: ExecutionContext = inFlightComps.ec
  implicit val messagesApi: MessagesApi = inFlightComps.messagesApi

  override def refine[A](request: User[A]): Future[Either[Result, User[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    implicit val req: User[A] = request

    req.session.get(inFlightContactDetailsChangeKey) match {
      case Some("false") => Future.successful(Right(req))
      case Some(change) => Future.successful(Left(Conflict(inFlightComps.inFlightChangeView(change))))
      case None => getCustomerInfoCall(req.vrn)
    }
  }

  private def getCustomerInfoCall[A](vrn: String)
                                    (implicit hc: HeaderCarrier, request: User[A]): Future[Either[Result, User[A]]] =
    inFlightComps.vatSubscriptionService.getCustomerInfo(vrn).map {
      case Right(customerInfo) =>
        customerInfo.pendingChanges match {
          case Some(changes) if changes.ppob.isDefined =>
            comparePendingAndCurrent(changes, customerInfo)
          case _ =>
            logDebug("[InFlightPredicate][getCustomerInfoCall] - There are no in-flight changes. " +
              "Redirecting user to the start of the journey.")
            Left(Redirect(redirectURL).addingToSession(inFlightContactDetailsChangeKey -> "false"))
        }
      case Left(error) =>
        logWarn("[InFlightPredicate][getCustomerInfoCall] - " +
          s"The call to the GetCustomerInfo API failed. Error: ${error.message}")
        Left(inFlightComps.errorHandler.showInternalServerError)
    }

  private def comparePendingAndCurrent[A](pendingChanges: PendingChanges, customerInfo: CustomerInformation)
                                         (implicit user: User[A]): Either[Result, User[A]] =

    (customerInfo.sameAddress, customerInfo.sameEmail, customerInfo.sameLandline,
      customerInfo.sameMobile, customerInfo.sameWebsite) match {
      case (false, _, _, _, _) =>
        Left(Conflict(inFlightComps.inFlightChangeView("ppob"))
          .addingToSession(inFlightContactDetailsChangeKey -> "ppob"))
      case (_, false, _, _, _) =>
        Left(Conflict(inFlightComps.inFlightChangeView("email"))
          .addingToSession(inFlightContactDetailsChangeKey -> "email"))
      case (_, _, false, _, _) =>
        Left(Conflict(inFlightComps.inFlightChangeView("landline"))
          .addingToSession(inFlightContactDetailsChangeKey -> "landline"))
      case (_, _, _, false, _) =>
        Left(Conflict(inFlightComps.inFlightChangeView("mobile"))
          .addingToSession(inFlightContactDetailsChangeKey -> "mobile"))
      case (_, _, _, _, false) =>
        Left(Conflict(inFlightComps.inFlightChangeView("website"))
          .addingToSession(inFlightContactDetailsChangeKey -> "website"))
      case _ => Right(user)
    }
}
