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

package testOnly.controllers

import config.ErrorHandler
import javax.inject.Inject
import play.api.libs.json.JsString
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import testOnly.connectors.RetrievePasscodeConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext

class RetrievePasscodeController @Inject()(val mcc: MessagesControllerComponents, errorHandler: ErrorHandler,
                                           retrievePasscodeConnector: RetrievePasscodeConnector)
                                          (implicit ec: ExecutionContext) extends FrontendController(mcc){

  def getPasscode: Action[AnyContent] = Action.async { implicit request =>
      retrievePasscodeConnector.getPasscode.map{
        case Right(model) => Ok(JsString(model.passcode))
        case _ => errorHandler.showInternalServerError
      }
  }
}
