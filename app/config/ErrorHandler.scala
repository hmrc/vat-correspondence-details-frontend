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

package config

import javax.inject.{Inject, Singleton}
import play.api.i18n.MessagesApi
import play.api.mvc.{Request, RequestHeader, Result}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.http.FrontendErrorHandler
import play.api.mvc.Results.InternalServerError
import views.html.errors.StandardErrorView

import scala.concurrent.Future

@Singleton
class ErrorHandler @Inject()(val messagesApi: MessagesApi,
                             standardErrorView: StandardErrorView,
                             implicit val appConfig: AppConfig) extends FrontendErrorHandler {

  private implicit def rhToRequest(rh: RequestHeader) : Request[_] = Request(rh, "")

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)
                                    (implicit request: Request[_]): Html =
    standardErrorView(pageTitle, heading, message)

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = exception match {
    case _ => Future.successful(showInternalServerError(request))
  }

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = statusCode match {
    case _ => Future.successful(showInternalServerError(request))
  }

  def showInternalServerError(implicit request: Request[_]): Result = {
    val msgs = request2Messages
    InternalServerError(standardErrorTemplate(
      msgs("standardError.title"),
      msgs("standardError.heading"),
      msgs("standardError.message")
    ))
  }
}
