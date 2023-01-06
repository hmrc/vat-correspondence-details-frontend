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

package controllers.predicates.inflight

import common.SessionKeys._
import config.AppConfig
import models.User
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.{Conflict, Redirect}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.LoggerUtil
import scala.concurrent.{ExecutionContext, Future}

class InFlightPredicate(inFlightComps: InFlightPredicateComponents,
                        redirectURL: String,
                        blockIfPendingPref: Boolean) extends ActionRefiner[User, User] with I18nSupport with LoggerUtil {

  implicit val appConfig: AppConfig = inFlightComps.appConfig
  implicit val executionContext: ExecutionContext = inFlightComps.mcc.executionContext
  implicit val messagesApi: MessagesApi = inFlightComps.messagesApi

  override def refine[A](request: User[A]): Future[Either[Result, User[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    implicit val req: User[A] = request

    req.session.get(inFlightContactDetailsChangeKey) match {
      case Some("false") => Future.successful(Right(req))
      case Some("commsPref") if blockIfPendingPref => Future.successful(Left(Conflict(inFlightComps.inFlightChangeView())))
      case Some("true") => Future.successful(Left(Conflict(inFlightComps.inFlightChangeView())))
      case _ => getCustomerInfoCall(req.vrn)
    }
  }

  private def getCustomerInfoCall[A](vrn: String)
                                    (implicit hc: HeaderCarrier, request: User[A]): Future[Either[Result, User[A]]] =
    inFlightComps.vatSubscriptionService.getCustomerInfo(vrn).map {
      case Right(customerInfo) =>
        customerInfo.pendingChanges match {
          case Some(changes) if blockIfPendingPref && changes.commsPreference.isDefined =>
            Left(Conflict(inFlightComps.inFlightChangeView()).addingToSession(inFlightContactDetailsChangeKey -> "commsPref"))
          case Some(changes) if changes.ppob.isDefined =>
            Left(Conflict(inFlightComps.inFlightChangeView()).addingToSession(inFlightContactDetailsChangeKey -> "true"))
          case _ =>
            logger.debug("[InFlightPredicate][getCustomerInfoCall] - There are no in-flight changes. " +
              "Redirecting user to the start of the journey.")
            Left(Redirect(redirectURL).addingToSession(
              inFlightContactDetailsChangeKey -> "false",
              validationEmailKey -> customerInfo.ppob.contactDetails.flatMap(_.emailAddress).getOrElse(""),
              validationLandlineKey -> customerInfo.ppob.contactDetails.flatMap(_.phoneNumber).getOrElse(""),
              validationMobileKey -> customerInfo.ppob.contactDetails.flatMap(_.mobileNumber).getOrElse(""),
              validationWebsiteKey -> customerInfo.ppob.websiteAddress.getOrElse("")
            ))
        }
      case Left(error) =>
        logger.warn("[InFlightPredicate][getCustomerInfoCall] - " +
          s"The call to the GetCustomerInfo API failed. Error: ${error.message}")
        Left(inFlightComps.errorHandler.showInternalServerError)
    }
}
