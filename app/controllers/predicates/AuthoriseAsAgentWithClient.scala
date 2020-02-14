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

package controllers.predicates

import javax.inject.{Inject, Singleton}
import common.{EnrolmentKeys, SessionKeys}
import config.{AppConfig, ErrorHandler}
import models.{Agent, User}
import play.api.i18n.MessagesApi
import play.api.mvc._
import services.EnrolmentsAuthService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve._
import utils.LoggerUtil.{logDebug, logWarn}
import views.html.errors.SessionTimeoutView
import views.html.errors.agent.AgentJourneyDisabledView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthoriseAsAgentWithClient @Inject()(enrolmentsAuthService: EnrolmentsAuthService,
                                           val errorHandler: ErrorHandler,
                                           mcc: MessagesControllerComponents,
                                           sessionTimeoutView: SessionTimeoutView,
                                           agentJourneyDisabledView: AgentJourneyDisabledView,
                                           implicit val appConfig: AppConfig,
                                           override implicit val executionContext: ExecutionContext,
                                           override val messagesApi: MessagesApi)

  extends AuthBasePredicate(mcc) with ActionBuilder[User, AnyContent] with ActionFunction[Request, User] {

  override val parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

  private def delegatedAuthRule(vrn: String): Enrolment =
    Enrolment(EnrolmentKeys.vatEnrolmentId)
      .withIdentifier(EnrolmentKeys.vatIdentifierId, vrn)
      .withDelegatedAuthRule(EnrolmentKeys.mtdVatDelegatedAuthRule)

  override def invokeBlock[A](request: Request[A], block: User[A] => Future[Result]): Future[Result] = {
    implicit val req: Request[A] = request

    request.session.get(SessionKeys.clientVrn) match {
      case Some(vrn) =>
        logDebug(s"[AuthoriseAsAgentWithClient][invokeBlock] - Client VRN from Session: $vrn")
        enrolmentsAuthService.authorised(delegatedAuthRule(vrn))
          .retrieve(v2.Retrievals.affinityGroup and v2.Retrievals.allEnrolments) {
            case None ~ _ =>
              Future.successful(errorHandler.showInternalServerError)
            case _ ~ allEnrolments =>
              val agent = Agent(allEnrolments)
              val user = User(vrn, active = true, Some(agent.arn))
              block(user)
          } recover {
            case _: NoActiveSession =>
              logDebug("[AuthoriseAsAgentWithClient][invokeBlock] - Agent does not have an active session, " +
                "rendering Session Timeout")
              Unauthorized(sessionTimeoutView())

            case _: AuthorisationException =>
              logWarn("[AuthoriseAsAgentWithClient][invokeBlock] - Agent does not have " +
                "delegated authority for Client")
              Redirect(appConfig.vatAgentClientLookupUnauthorised)

          }
      case _ =>
        Future.successful(Redirect(appConfig.vatAgentClientLookupServicePath))
    }
  }
}
