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

package controllers.contactNumbers

import common.SessionKeys
import config.{AppConfig, ErrorHandler}
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.ContactNumbersForm._
import javax.inject.{Inject, Singleton}
import models.customerInformation.ContactNumbers
import play.api.mvc._
import services.VatSubscriptionService
import views.html.contactNumbers.CaptureLandlineNumberView
import views.html.errors.NotFoundView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureLandlineNumberController @Inject()(val vatSubscriptionService: VatSubscriptionService,
                                                val errorHandler: ErrorHandler,
                                                captureLandlineNumberView: CaptureLandlineNumberView,
                                                notFoundView: NotFoundView)
                                               (implicit val appConfig: AppConfig,
                                                mcc: MessagesControllerComponents,
                                                authComps: AuthPredicateComponents,
                                                inFlightComps: InFlightPredicateComponents) extends BaseController {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (allowAgentPredicate andThen inFlightContactNumbersPredicate).async { implicit user =>

    if(appConfig.features.changeContactDetailsEnabled()) {

      val validationNumbers: Future[(Option[String], Option[String])] =
        (user.session.get(SessionKeys.validationLandlineKey), user.session.get(SessionKeys.validationMobileKey)) match {
          case (Some(landline), Some(mobile)) => Future.successful(Some(landline), Some(mobile))
          case _ =>
            vatSubscriptionService.getCustomerInfo(user.vrn).map {
              case Right(details) => (
                Some(details.ppob.contactDetails.flatMap(_.phoneNumber).getOrElse("")),
                Some(details.ppob.contactDetails.flatMap(_.mobileNumber).getOrElse(""))
              )
              case _ => (None, None)
            }
        }

      val prepopulationLandline: Future[String] = validationNumbers map { validationNumbers =>
        user.session.get(SessionKeys.prepopulationLandlineKey).getOrElse(validationNumbers._1.getOrElse(""))
      }

      val prepopulationMobile: Future[String] = validationNumbers map { validationNumbers =>
        user.session.get(SessionKeys.prepopulationMobileKey).getOrElse(validationNumbers._2.getOrElse(""))
      }

      for {
        (validationLandline, validationMobile) <- validationNumbers
        prepopulationLandline <- prepopulationLandline
        prepopulationMobile <- prepopulationMobile
      } yield {
        (validationLandline, validationMobile) match {
          case (Some(valLandline), Some(valMobile)) =>
            Ok(captureLandlineNumberView(contactNumbersForm(valLandline, valMobile).fill(
              ContactNumbers(Some(prepopulationLandline), Some(prepopulationMobile))
            ))).addingToSession(
              SessionKeys.validationLandlineKey -> valLandline,
              SessionKeys.validationMobileKey -> valMobile
            )
          case _ => errorHandler.showInternalServerError
        }
      }
    } else {
      Future.successful(NotFound(notFoundView()))
    }
  }

  def submit: Action[AnyContent] = (allowAgentPredicate andThen inFlightContactNumbersPredicate) { implicit user =>
    val validationLandline: Option[String] = user.session.get(SessionKeys.validationLandlineKey)
    val prepopulationLandline: Option[String] = user.session.get(SessionKeys.prepopulationLandlineKey)
    val validationMobile: Option[String] = user.session.get(SessionKeys.validationMobileKey)
    val prepopulationMobile: Option[String] = user.session.get(SessionKeys.prepopulationMobileKey)

    (validationLandline, validationMobile) match {
      case (Some(validationLand), Some(validationMob)) =>
        contactNumbersForm(validationLand, validationMob).bindFromRequest.fold(
          errorForm => {
            BadRequest(captureLandlineNumberView(errorForm))
          },
          contactDetails => {
            Redirect(routes.ConfirmContactNumbersController.show()).addingToSession(
              SessionKeys.prepopulationLandlineKey -> contactDetails.landlineNumber.getOrElse(""),
              SessionKeys.prepopulationMobileKey -> contactDetails.mobileNumber.getOrElse("")
            )
          }
        )
      case _ => errorHandler.showInternalServerError
    }
  }
}
