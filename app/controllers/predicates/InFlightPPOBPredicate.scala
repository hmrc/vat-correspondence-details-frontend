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

package controllers.predicates

import javax.inject.{Inject, Singleton}

import common.SessionKeys.inFlightContactDetailsChangeKey
import config.{AppConfig, ErrorHandler}
import models.User
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.{Ok, Redirect}
import services.VatSubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InFlightPPOBPredicate @Inject()(vatSubscriptionService: VatSubscriptionService,
                                      val errorHandler: ErrorHandler,
                                      val messagesApi: MessagesApi,
                                      implicit val appConfig: AppConfig,
                                      implicit val ec: ExecutionContext)
  extends ActionRefiner[User, User] with I18nSupport {

  override def refine[A](request: User[A]): Future[Either[Result, User[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    implicit val req: User[A] = request

    req.session.get(inFlightContactDetailsChangeKey) match {
      case Some("true") => Future.successful(Left(Ok(views.html.errors.ppobChangePending())))
      case Some("false") => Future.successful(Right(req))
      case Some(_) => Future.successful(Left(errorHandler.showInternalServerError))
      case None => getCustomerInfoCall(req.vrn)
    }
  }

  private def getCustomerInfoCall[A](vrn: String)(implicit hc: HeaderCarrier,
                                                  request: User[A]): Future[Either[Result, User[A]]] =
    vatSubscriptionService.getCustomerInfo(vrn).map {
      case Right(customerInfo) =>
        customerInfo.pendingChanges match {
          case Some(_) =>
            (customerInfo.approvedAndPendingPPOBAddressMatch, customerInfo.approvedAndPendingEmailAddressMatch) match {
              case (false, true) =>
                Logger.warn("[InFlightPPOBPredicate][getCustomerInfoCall] - " +
                  "There is an in-flight PPOB address change. Rendering graceful error page.")
                Left(Ok(views.html.errors.ppobChangePending()).addingToSession(inFlightContactDetailsChangeKey -> "true"))
              case (_, false) =>
                Logger.warn("[InFlightPPOBPredicate][getCustomerInfoCall] - " +
                  "There is an in-flight email address change. Redirecting to Manage VAT homepage")
                Left(Redirect(appConfig.manageVatSubscriptionServicePath).addingToSession(inFlightContactDetailsChangeKey -> "true"))
              case (_, _) =>
                Logger.warn("[InFlightPPOBPredicate][getCustomerInfoCall] - " +
                  "There is an in-flight contact details change that is not PPOB or email address. Rendering standard error page.")
                Left(errorHandler.showInternalServerError.addingToSession(inFlightContactDetailsChangeKey -> "error"))
            }
          case None =>
            Logger.debug("[InFlightPPOBPredicate][getCustomerInfoCall] - There is no in-flight change. Redirecting user to the start of the journey.")
            Left(Redirect(controllers.routes.CaptureEmailController.show().url).addingToSession(inFlightContactDetailsChangeKey -> "false"))
        }
      case Left(error) =>
        Logger.warn(s"[InFlightPPOBPredicate][getCustomerInfoCall] - The call to the GetCustomerInfo API failed. Error: ${error.message}")
        Left(errorHandler.showInternalServerError)
    }
}