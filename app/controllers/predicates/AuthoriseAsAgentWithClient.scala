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

import common.{EnrolmentKeys, SessionKeys}
import config.{AppConfig, ErrorHandler}
import javax.inject.{Inject, Singleton}
import models.{Agent, User}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.EnrolmentsAuthService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Retrievals, ~}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

@Singleton
class AuthoriseAsAgentWithClient @Inject()(enrolmentsAuthService: EnrolmentsAuthService,
                                           val serviceErrorHandler: ErrorHandler,
                                           implicit val messagesApi: MessagesApi,
                                           implicit val appConfig: AppConfig)
  extends FrontendController with AuthBasePredicate with I18nSupport with ActionBuilder[User] with ActionFunction[Request, User] {

  private def delegatedAuthRule(vrn: String): Enrolment =
    Enrolment(EnrolmentKeys.vatEnrolmentId)
      .withIdentifier(EnrolmentKeys.vatIdentifierId, vrn)
      .withDelegatedAuthRule(EnrolmentKeys.mtdVatDelegatedAuthRule)

  override def invokeBlock[A](request: Request[A], block: User[A] => Future[Result]): Future[Result] = {
    implicit val req: Request[A] = request

    request.session.get(SessionKeys.clientVRN) match {
      case Some(vrn) =>
        Logger.debug(s"[AuthoriseAsAgentWithClient][invokeBlock] - Client VRN from Session: $vrn")
        enrolmentsAuthService.authorised(delegatedAuthRule(vrn)).retrieve(Retrievals.affinityGroup and Retrievals.allEnrolments) {
          case None ~ _ =>
            // TODO Add error service handler
            Future.successful(InternalServerError)
          case _ ~ allEnrolments =>
            val agent = Agent(allEnrolments)
            val user = User(vrn, active = true, Some(agent.arn))
            block(user)
        } recover {
          case _: NoActiveSession =>
            Logger.debug(s"[AuthoriseAsAgentWithClient][invokeBlock] - Agent does not have an active session, rendering Session Timeout")
            // TODO Add unauthorised view
            Unauthorized("Unauthorised")
          case _: AuthorisationException =>
            Logger.warn(s"[AuthoriseAsAgentWithClient][invokeBlock] - Agent does not have delegated authority for Client")
            // TODO Redirect to VACLUF service
            Redirect(controllers.routes.HelloWorldController.helloWorld())
        }
      case _ =>
        Logger.warn(s"[AuthoriseAsAgentWithClient][invokeBlock] - No Client VRN in session, redirecting to Select Client page")
        // TODO Redirect to VACLUF service
        Future.successful(Redirect(controllers.routes.HelloWorldController.helloWorld()))
    }
  }
}
