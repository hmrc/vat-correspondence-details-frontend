/*
 * Copyright 2021 HM Revenue & Customs
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
import config.AppConfig
import models.User
import play.api.i18n.MessagesApi
import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.{AuthorisationException, Enrolments, NoActiveSession}
import utils.LoggerUtil.{logDebug, logWarn}

import scala.concurrent.{ExecutionContext, Future}

class AuthPredicate(authComps: AuthPredicateComponents,
                    allowsAgents: Boolean,
                    isChangePrefJourney: Boolean = false)
  extends AuthBasePredicate(authComps.mcc) with ActionBuilder[User, AnyContent] with ActionFunction[Request, User] {

  override val parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser
  override implicit val messagesApi: MessagesApi = authComps.messagesApi

  implicit val appConfig: AppConfig = authComps.appConfig
  implicit val executionContext: ExecutionContext = authComps.executionContext

  override def invokeBlock[A](request: Request[A], block: User[A] => Future[Result]): Future[Result] = {

    implicit val req: Request[A] = request

    authComps.enrolmentsAuthService.authorised().retrieve(v2.Retrievals.affinityGroup and v2.Retrievals.allEnrolments) {
      case Some(affinityGroup) ~ allEnrolments =>
        (isAgent(affinityGroup), allEnrolments) match {
          case (true, enrolments) =>
            if (appConfig.features.agentAccessEnabled() && allowsAgents) {
              checkAgentEnrolment(enrolments, block)
            } else if(isChangePrefJourney) {
              Future.successful(Redirect(authComps.appConfig.vatAgentClientLookupAgentHubPath))
            } else {
              Future.successful(Unauthorized(authComps.agentJourneyDisabledView()))
            }
          case (false, enrolments) => checkVatEnrolment(enrolments, block)
        }
      case _ =>
        logWarn("[AuthPredicate][invokeBlock] - Missing affinity group")
        Future.successful(authComps.errorHandler.showInternalServerError)
    } recover {
      case _: NoActiveSession =>
        logDebug("[AuthPredicate][invokeBlock] - No active session, rendering Session Timeout view")
        Unauthorized(authComps.sessionTimeoutView())

      case _: AuthorisationException =>
        logWarn("[AuthPredicate][invokeBlock] - Unauthorised exception, rendering standard error view")
        authComps.errorHandler.showInternalServerError
    }
  }

  private[AuthPredicate] def checkAgentEnrolment[A](enrolments: Enrolments, block: User[A] => Future[Result])
                                                   (implicit request: Request[A]) =
    if (enrolments.enrolments.exists(_.key == EnrolmentKeys.agentEnrolmentId)) {
      logDebug("[AuthPredicate][checkAgentEnrolment] - Authenticating as agent")
      authComps.authenticateAsAgentWithClient.invokeBlock(request, block)
    } else {
      logDebug(s"[AuthPredicate][checkAgentEnrolment] - Agent without HMRC-AS-AGENT enrolment. Enrolments: $enrolments")
      logWarn(s"[AuthPredicate][checkAgentEnrolment] - Agent without HMRC-AS-AGENT enrolment.")
      Future.successful(Forbidden(authComps.unauthorisedAgentView()))
    }

  private[AuthPredicate] def checkVatEnrolment[A](enrolments: Enrolments, block: User[A] => Future[Result])
                                                 (implicit request: Request[A]) =
    if (enrolments.enrolments.exists(_.key == EnrolmentKeys.vatEnrolmentId)) {

      val user = User(enrolments)

      request.session.get(SessionKeys.insolventWithoutAccessKey) match {
        case Some("true") => Future.successful(Forbidden(authComps.notSignedUpView()))
        case Some("false") => block(user)
        case _ => authComps.vatSubscriptionService.getCustomerInfo(user.vrn).flatMap {
          case Right(info) if info.isInsolventWithoutAccess =>
            logDebug("[AuthPredicate][checkVatEnrolment] - User is insolvent and not continuing to trade")
            Future.successful(Forbidden(authComps.notSignedUpView())
              .addingToSession(SessionKeys.insolventWithoutAccessKey -> "true"))
          case Right(_) =>
            logDebug("[AuthPredicate][checkVatEnrolment] - Authenticated as principle")
            block(user).map(result => result
              .addingToSession(SessionKeys.insolventWithoutAccessKey -> "false"))
          case _ =>
            logDebug("[AuthPredicate][checkVatEnrolment] - " +
              "Failure obtaining insolvency status from Customer Info API")
            Future.successful(authComps.errorHandler.showInternalServerError)
        }
      }
    } else {
      logDebug(s"[AuthPredicate][checkVatEnrolment] - Non-agent without HMRC-MTD-VAT enrolment. $enrolments")
      Future.successful(Forbidden(authComps.notSignedUpView()))
    }
}
