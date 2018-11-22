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

import common.{EnrolmentKeys, SessionKeys}
import config.{AppConfig, ErrorHandler}
import models.{Agent, User}
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.mvc._
import services.EnrolmentsAuthService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve._

import scala.concurrent.Future

@Singleton
class AuthoriseAsAgentWithClient @Inject()(enrolmentsAuthService: EnrolmentsAuthService,
                                           val errorHandler: ErrorHandler,
                                           implicit val messagesApi: MessagesApi,
                                           implicit val appConfig: AppConfig)
  extends AuthBasePredicate with ActionBuilder[User] with ActionFunction[Request, User] {

  private def delegatedAuthRule(vrn: String): Enrolment =
    Enrolment(EnrolmentKeys.vatEnrolmentId)
      .withIdentifier(EnrolmentKeys.vatIdentifierId, vrn)
      .withDelegatedAuthRule(EnrolmentKeys.mtdVatDelegatedAuthRule)

    override def invokeBlock[A](request: Request[A], block: User[A] => Future[Result]): Future[Result] = {
      implicit val req: Request[A] = request

      if (appConfig.features.agentAccessEnabled()) {
        request.session.get(SessionKeys.clientVrn) match {
          case Some(vrn) =>
            Logger.debug(s"[AuthoriseAsAgentWithClient][invokeBlock] - Client VRN from Session: $vrn")
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
                  Logger.debug(s"[AuthoriseAsAgentWithClient][invokeBlock] - Agent does not have an active session, " +
                    s"rendering Session Timeout")
                  Unauthorized(views.html.errors.sessionTimeout())

                case _: AuthorisationException =>
                  Logger.warn(s"[AuthoriseAsAgentWithClient][invokeBlock] - Agent does not have " +
                    s"delegated authority for Client")
                  Ok(views.html.errors.agent.notAuthorisedForClient(vrn))

              }
          case _ =>
            //TODO redirect to VALCUFE with query string for redirectURL
            Future.successful(errorHandler.showInternalServerError)
        }
      } else {
        Future.successful(Unauthorized(views.html.errors.agent.agentJourneyDisabled()))
      }
    }
  }
