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

import common.EnrolmentKeys
import config.{AppConfig, ErrorHandler}
import models.User
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.mvc.{ActionBuilder, ActionFunction, Request, Result}
import services.EnrolmentsAuthService
import uk.gov.hmrc.auth.core.{AuthorisationException, Enrolments, NoActiveSession}
import uk.gov.hmrc.auth.core.retrieve._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthPredicate @Inject()(enrolmentsAuthService: EnrolmentsAuthService,
                              val messagesApi: MessagesApi,
                              val errorHandler: ErrorHandler,
                              val authenticateAsAgentWithClient: AuthoriseAsAgentWithClient,
                              implicit val appConfig: AppConfig,
                              implicit val ec: ExecutionContext)
  extends AuthBasePredicate with ActionBuilder[User] with ActionFunction[Request, User] {

  override def invokeBlock[A](request: Request[A], block: User[A] => Future[Result]): Future[Result] = {

    implicit val req: Request[A] = request
    enrolmentsAuthService.authorised().retrieve(v2.Retrievals.affinityGroup and v2.Retrievals.allEnrolments) {
      case Some(affinityGroup) ~ allEnrolments =>
        (isAgent(affinityGroup), allEnrolments) match {
          case (true, enrolments) =>
            if (appConfig.features.agentAccessEnabled()) {
              checkAgentEnrolment(enrolments, block)
            } else {
              Future.successful(Unauthorized(views.html.errors.agent.agentJourneyDisabled()))
            }
          case (false, enrolments) => checkVatEnrolment(enrolments, block)
        }
      case _ =>
        Logger.warn("[AuthPredicate][invokeBlock] - Missing affinity group")
        Future.successful(errorHandler.showInternalServerError)
    } recover {
      case _: NoActiveSession =>
        Logger.debug("[AuthPredicate][invokeBlock] - No active session, rendering Session Timeout view")
        Unauthorized(views.html.errors.sessionTimeout())

      case _: AuthorisationException =>
        Logger.warn("[AuthPredicate][invokeBlock] - Unauthorised exception, rendering Unauthorised view")
        errorHandler.showInternalServerError
    }
  }

  private[AuthPredicate] def checkAgentEnrolment[A](enrolments: Enrolments, block: User[A] => Future[Result])(implicit request: Request[A]) =
    if (enrolments.enrolments.exists(_.key == EnrolmentKeys.agentEnrolmentId)) {
      Logger.debug("[AuthPredicate][checkAgentEnrolment] - Authenticating as agent")
      authenticateAsAgentWithClient.invokeBlock(request, block)
    }
    else {
      Logger.debug(s"[AuthPredicate][checkAgentEnrolment] - Agent without HMRC-AS-AGENT enrolment. Enrolments: $enrolments")
      Logger.warn(s"[AuthPredicate][checkAgentEnrolment] - Agent without HMRC-AS-AGENT enrolment.")
      Future.successful(Forbidden(views.html.errors.agent.unauthorisedAgent()))
    }

  private[AuthPredicate] def checkVatEnrolment[A](enrolments: Enrolments, block: User[A] => Future[Result])(implicit request: Request[A]) =
    if (enrolments.enrolments.exists(_.key == EnrolmentKeys.vatEnrolmentId)) {
      Logger.debug("[AuthPredicate][checkVatEnrolment] - Authenticated as principle")
      block(User(enrolments))
    }
    else {
      Logger.debug(s"[AuthPredicate][checkVatEnrolment] - Non-agent without HMRC-MTD-VAT enrolment. $enrolments")
      Future.successful(Forbidden(views.html.errors.not_signed_up()))
    }
}

