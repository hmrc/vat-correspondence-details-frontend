/*
 * Copyright 2018 HM Revenue & Customs
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

import common.SessionKeys.inflightPPOBKey
import config.{AppConfig, ErrorHandler}
import models.User
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{ActionBuilder, ActionFunction, Request, Result}
import services.{EnrolmentsAuthService, VatSubscriptionService}
import uk.gov.hmrc.auth.core.retrieve.{Retrievals, ~}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

@Singleton
class InflightPPOBPredicate @Inject()(vatSubscriptionService: VatSubscriptionService,
                                      enrolmentsAuthService: EnrolmentsAuthService,
                                      val errorHandler: ErrorHandler,
                                      val messagesApi: MessagesApi,
                                      implicit val appConfig: AppConfig) extends FrontendController
  with AuthBasePredicate with I18nSupport with ActionBuilder[User] with ActionFunction[Request, User] {

  override def invokeBlock[A](request: Request[A], block: User[A] => Future[Result]): Future[Result] = {

    implicit val req: Request[A] = request

    val authorisedUser: Future[User[A]] =
      enrolmentsAuthService.authorised().retrieve(Retrievals.affinityGroup and Retrievals.allEnrolments) {
        case Some(_) ~ enrolments => Future.successful(User(enrolments))
      }

    authorisedUser.flatMap { user =>
      request.session.get(inflightPPOBKey) match {
        case Some("true") => Future.successful(Ok(views.html.errors.ppobChangePending()))
        case Some("false") => block(user)
        case Some(_) => Future.successful(errorHandler.showInternalServerError)
        case None => getCustomerInfoCall(user.vrn, block)
      }
    }
  }

  private def getCustomerInfoCall[A](vrn: String, block: User[A] => Future[Result])
                                    (implicit request: Request[A]): Future[Result] =
    vatSubscriptionService.getCustomerInfo(vrn).flatMap {
      case Right(customerInfo) =>
        customerInfo.pendingChanges match {
          case Some(_) =>
            if(!customerInfo.addressAndPendingMatch) {
              Logger.warn("[InflightPPOBPredicate][invokeBlock] - " +
                "The current PPOB address and inflight PPOB address are different. Rendering graceful error page.")
              Future.successful(Ok(views.html.errors.ppobChangePending()).addingToSession(inflightPPOBKey -> "true"))
            } else {
              Logger.warn("[InflightPPOBPredicate][invokeBlock] - " +
                "There is a pending change to something other than PPOB address. Rendering standard error page.")
              Future.successful(errorHandler.showInternalServerError.addingToSession(inflightPPOBKey -> "error"))
            }
          case None =>
            Logger.debug("[InflightPPOBPredicate][invokeBlock] - There is no inflight data.")
            block(User(vrn)).map(_.addingToSession(inflightPPOBKey -> "false"))
        }
      case Left(error) =>
        Logger.warn(s"[InflightPPOBPredicate][invokeBlock] - " +
          s"The call to the GetCustomerInfo API failed. Error: ${error.body}")
        Future.successful(errorHandler.showInternalServerError)
    }
}