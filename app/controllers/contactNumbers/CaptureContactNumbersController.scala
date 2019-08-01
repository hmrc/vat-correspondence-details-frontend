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
import forms.ContactNumbersForm._
import javax.inject.{Inject, Singleton}
import models.customerInformation.ContactNumbers
import play.api.mvc._
import services.VatSubscriptionService
import views.html.contactNumbers.CaptureContactNumbersView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureContactNumbersController @Inject()(val authComps: AuthPredicateComponents,
                                                override val mcc: MessagesControllerComponents,
                                                val vatSubscriptionService: VatSubscriptionService,
                                                val errorHandler: ErrorHandler,
                                                captureContactNumbersView: CaptureContactNumbersView,
                                                implicit val appConfig: AppConfig) extends BaseController(mcc, authComps) {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = allowAgentPredicate.async { implicit user =>

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
      validationLandline    <- validationNumbers map {numbers => numbers._1}
      validationMobile      <- validationNumbers map {numbers => numbers._2}
      prepopulationLandline <- prepopulationLandline
      prepopulationMobile   <- prepopulationMobile
    } yield {
      (validationLandline, validationMobile) match {
        case (Some(valLandline), Some(valMobile)) =>
          Ok(captureContactNumbersView(contactNumbersForm(valLandline, valMobile).fill(
            ContactNumbers(Some(prepopulationLandline), Some(prepopulationMobile))
          ))).addingToSession(
            SessionKeys.validationLandlineKey -> valLandline,
            SessionKeys.validationMobileKey -> valMobile
          )
        case _ => errorHandler.showInternalServerError
      }
    }
  }

  def submit: Action[AnyContent] = allowAgentPredicate { implicit user =>
    val validationLandline: Option[String] = user.session.get(SessionKeys.validationLandlineKey)
    val prepopulationLandline: Option[String] = user.session.get(SessionKeys.prepopulationLandlineKey)
    val validationMobile: Option[String] = user.session.get(SessionKeys.validationMobileKey)
    val prepopulationMobile: Option[String] = user.session.get(SessionKeys.prepopulationMobileKey)

    (validationLandline, prepopulationLandline, validationMobile, prepopulationMobile) match {
      case (Some(validationLand), Some(_), Some(validationMob), Some(_)) =>
        contactNumbersForm(validationLand, validationMob).bindFromRequest.fold(
          errorForm => {
            BadRequest(captureContactNumbersView(errorForm))
          },
          //TODO: direct this to ConfirmContactNumbersController.show()
          contactDetails => {
            Redirect(routes.CaptureContactNumbersController.show()).addingToSession(
              SessionKeys.prepopulationLandlineKey -> contactDetails.landlineNumber.getOrElse(""),
              SessionKeys.prepopulationMobileKey -> contactDetails.mobileNumber.getOrElse("")
            )
          }
        )
      case _ => errorHandler.showInternalServerError
    }
  }
}
