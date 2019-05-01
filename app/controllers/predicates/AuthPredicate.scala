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
import common.EnrolmentKeys
import config.{AppConfig, ErrorHandler}
import models.User
import play.api.mvc._
import services.EnrolmentsAuthService
import uk.gov.hmrc.auth.core.{AuthorisationException, Enrolments, NoActiveSession}
import uk.gov.hmrc.auth.core.retrieve._
import utils.LoggerUtil.{logDebug, logWarn}
import views.html.errors.{NotSignedUpView, SessionTimeoutView}
import views.html.errors.agent.{AgentJourneyDisabledView, UnauthorisedAgentView}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthPredicate @Inject()(enrolmentsAuthService: EnrolmentsAuthService,
                              override val mcc: MessagesControllerComponents,
                              val errorHandler: ErrorHandler,
                              val authenticateAsAgentWithClient: AuthoriseAsAgentWithClient,
                              sessionTimeoutView: SessionTimeoutView,
                              agentJourneyDisabledView: AgentJourneyDisabledView,
                              unauthorisedAgentView: UnauthorisedAgentView,
                              notSignedUpView: NotSignedUpView,
                              implicit val appConfig: AppConfig,
                              override implicit val executionContext: ExecutionContext)
  extends AuthBasePredicate(mcc) with ActionBuilder[User, AnyContent] with ActionFunction[Request, User] {

  override val parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

  override def invokeBlock[A](request: Request[A], block: User[A] => Future[Result]): Future[Result] = {

    implicit val req: Request[A] = request
    enrolmentsAuthService.authorised().retrieve(v2.Retrievals.affinityGroup and v2.Retrievals.allEnrolments) {
      case Some(affinityGroup) ~ allEnrolments =>
        (isAgent(affinityGroup), allEnrolments) match {
          case (true, enrolments) =>
            if (appConfig.features.agentAccessEnabled()) {
              checkAgentEnrolment(enrolments, block)
            } else {
              Future.successful(Unauthorized(agentJourneyDisabledView()))
            }
          case (false, enrolments) => checkVatEnrolment(enrolments, block)
        }
      case _ =>
        logWarn("[AuthPredicate][invokeBlock] - Missing affinity group")
        Future.successful(errorHandler.showInternalServerError)
    } recover {
      case _: NoActiveSession =>
        logDebug("[AuthPredicate][invokeBlock] - No active session, rendering Session Timeout view")
        Unauthorized(sessionTimeoutView())

      case _: AuthorisationException =>
        logWarn("[AuthPredicate][invokeBlock] - Unauthorised exception, rendering standard error view")
        errorHandler.showInternalServerError
    }
  }

  private[AuthPredicate] def checkAgentEnrolment[A](enrolments: Enrolments, block: User[A] => Future[Result])(implicit request: Request[A]) =
    if (enrolments.enrolments.exists(_.key == EnrolmentKeys.agentEnrolmentId)) {
      logDebug("[AuthPredicate][checkAgentEnrolment] - Authenticating as agent")
      authenticateAsAgentWithClient.invokeBlock(request, block)
    }
    else {
      logDebug(s"[AuthPredicate][checkAgentEnrolment] - Agent without HMRC-AS-AGENT enrolment. Enrolments: $enrolments")
      logWarn(s"[AuthPredicate][checkAgentEnrolment] - Agent without HMRC-AS-AGENT enrolment.")
      Future.successful(Forbidden(unauthorisedAgentView()))
    }

  private[AuthPredicate] def checkVatEnrolment[A](enrolments: Enrolments, block: User[A] => Future[Result])(implicit request: Request[A]) =
    if (enrolments.enrolments.exists(_.key == EnrolmentKeys.vatEnrolmentId)) {
      logDebug("[AuthPredicate][checkVatEnrolment] - Authenticated as principle")
      block(User(enrolments))
    }
    else {
      logDebug(s"[AuthPredicate][checkVatEnrolment] - Non-agent without HMRC-MTD-VAT enrolment. $enrolments")
      Future.successful(Forbidden(notSignedUpView()))
    }
}
