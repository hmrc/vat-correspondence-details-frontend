/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.website

import audit.AuditingService
import audit.models.ChangedWebsiteAddressAuditModel
import common.SessionKeys._
import config.{AppConfig, ErrorHandler}
import controllers.predicates.AuthPredicateComponents
import controllers.BaseController
import controllers.predicates.inflight.InFlightPredicateComponents
import forms.YesNoForm
import models.{No, User, Yes, YesNo}

import javax.inject.{Inject, Singleton}
import models.errors.ErrorModel
import models.viewModels.CheckYourAnswersViewModel
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.VatSubscriptionService
import utils.LoggerUtil
import views.html.templates.CheckYourAnswersView
import views.html.website.ConfirmRemoveWebsiteView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConfirmWebsiteController @Inject()(errorHandler: ErrorHandler,
                                         vatSubscriptionService: VatSubscriptionService,
                                         confirmWebsiteView: CheckYourAnswersView,
                                         confirmRemoveWebsite: ConfirmRemoveWebsiteView,
                                         auditService: AuditingService)
                                        (implicit appConfig: AppConfig,
                                         mcc: MessagesControllerComponents,
                                         authComps: AuthPredicateComponents,
                                         inFlightComps: InFlightPredicateComponents) extends BaseController with LoggerUtil {

  implicit val ec: ExecutionContext = mcc.executionContext

  def show: Action[AnyContent] = (allowAgentPredicate andThen inFlightWebsitePredicate) { implicit user =>

    user.session.get(prepopulationWebsiteKey).filter(_.nonEmpty) match {
      case Some(website) =>
        Ok(
          confirmWebsiteView(CheckYourAnswersViewModel(
            question = "checkYourAnswers.websiteAddress",
            answer = website,
            changeLink = routes.CaptureWebsiteController.show.url,
            changeLinkHiddenText = "checkYourAnswers.websiteAddress.edit",
            continueLink = routes.ConfirmWebsiteController.updateWebsite
          )
        ))
      case _ =>
        Redirect(routes.CaptureWebsiteController.show)
    }
  }

  private[controllers] def performUpdate(newWebsite: String, pageUrl: String)(implicit user: User[_]): Future[Result] =
    vatSubscriptionService.updateWebsite(user.vrn, newWebsite) map {
      case Right(_) =>
        auditService.extendedAudit(
          ChangedWebsiteAddressAuditModel(
            user.session.get(validationWebsiteKey).filter(_.nonEmpty),
            newWebsite,
            user.vrn,
            user.isAgent,
            user.arn
          ),
          pageUrl
        )
        Redirect(controllers.routes.ChangeSuccessController.websiteAddress)
          .addingToSession(websiteChangeSuccessful -> "true", inFlightContactDetailsChangeKey -> "true")
          .removingFromSession(validationWebsiteKey, prepopulationWebsiteKey)

      case Left(ErrorModel(CONFLICT, _)) =>
        logger.warn("[ConfirmWebsiteController][performUpdate] - There is a contact details update request " +
          "already in progress. Redirecting user to manage-vat overview page.")
        Redirect(appConfig.manageVatSubscriptionServicePath)
          .addingToSession(inFlightContactDetailsChangeKey -> "true")

      case Left(_) =>
        errorHandler.showInternalServerError
    }

  def updateWebsite: Action[AnyContent] = (allowAgentPredicate andThen inFlightWebsitePredicate).async { implicit user =>
    user.session.get(prepopulationWebsiteKey) match {
      case Some(website) =>
        performUpdate(website, controllers.website.routes.ConfirmWebsiteController.updateWebsite.url)
      case _ =>
        Future.successful(Redirect(routes.CaptureWebsiteController.show))
    }
  }

  val formYesNo: Form[YesNo] = YesNoForm.yesNoForm("confirmWebsiteRemove.error")

  def removeShow: Action[AnyContent] = (allowAgentPredicate andThen inFlightWebsitePredicate) { implicit user =>
    user.session.get(validationWebsiteKey).filter(_.nonEmpty) match {
      case Some(_) =>
        Ok(confirmRemoveWebsite(formYesNo))
      case _ =>
        Redirect(routes.CaptureWebsiteController.show)
    }
  }

  def removeWebsiteAddress: Action[AnyContent] = (allowAgentPredicate andThen
                                                    inFlightWebsitePredicate).async { implicit user =>
    user.session.get(validationWebsiteKey).filter(_.nonEmpty) match {
      case Some(_) =>
        formYesNo.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(confirmRemoveWebsite(formWithErrors))),
          {
            case Yes => performUpdate("", controllers.website.routes.ConfirmWebsiteController.removeWebsiteAddress.url)
            case No => Future.successful(Redirect(appConfig.manageVatSubscriptionServicePath))
          }
        )
      case _ =>
        Future.successful(Redirect(routes.CaptureWebsiteController.show))
    }
  }
}
