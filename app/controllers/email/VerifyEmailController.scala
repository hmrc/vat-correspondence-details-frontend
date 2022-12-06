/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.email

import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthPredicateComponents
import controllers.BaseController
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.{Inject, Singleton}
import models.User
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{EmailVerificationService, VatSubscriptionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.LoggerUtil
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifyEmailController @Inject()(val emailVerificationService: EmailVerificationService,
                                      val errorHandler: ErrorHandler,
                                      vatSubscriptionService: VatSubscriptionService)
                                     (implicit val appConfig: AppConfig,
                                      mcc: MessagesControllerComponents,
                                      authComps: AuthPredicateComponents,
                                      inFlightComps: InFlightPredicateComponents) extends BaseController with LoggerUtil {

  implicit val ec: ExecutionContext = mcc.executionContext

  def emailSendVerification: Action[AnyContent] = blockAgentPredicate.async {

    Future.successful(Redirect(routes.VerifyPasscodeController.emailSendVerification))

  }

  def btaVerifyEmailRedirect: Action[AnyContent] = blockAgentPredicate.async {
    implicit user =>

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(user, user.session)

      if(user.headers.get("Referer").getOrElse("").contains(appConfig.btaAccountDetailsUrl)){
        vatSubscriptionService.getCustomerInfo(user.vrn) map {
          case Right(details) => (details.approvedEmail, details.ppob.contactDetails.flatMap(_.emailVerified)) match {
            case (Some(_), Some(true)) =>
              logger.debug("[EmailVerificationController][btaVerifyEmailRedirect] - emailVerified has come back as true. Returning user to BTA")
              Redirect(appConfig.btaAccountDetailsUrl)
            case (Some(email), _)  => Redirect(routes.VerifyPasscodeController.emailSendVerification)
              .addingToSession(SessionKeys.prepopulationEmailKey -> email)
              .addingToSession(SessionKeys.inFlightContactDetailsChangeKey -> s"${details.pendingPpobChanges}")
            case (_, _) =>
              logger.debug("[EmailVerificationController][btaVerifyEmailRedirect] - user does not have an email. Redirecting to capture email page")
              Redirect(routes.CaptureEmailController.show)
          }
          case _ => errorHandler.showInternalServerError
        }
      } else {
        logger.debug("[EmailVerificationController][btaVerifyEmailRedirect] - user has not come from BTA account details page. Throwing ISE")
        Future.successful(errorHandler.showInternalServerError)
      }

  }

  private[controllers] def extractSessionEmail(implicit user: User[AnyContent]): Option[String] = {
    user.session.get(SessionKeys.prepopulationEmailKey).filter(_.nonEmpty).orElse(None)
  }

}
