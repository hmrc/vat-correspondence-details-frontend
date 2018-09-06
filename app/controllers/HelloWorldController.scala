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

package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._

import scala.concurrent.Future
import play.api.i18n.{I18nSupport, MessagesApi}
import config.AppConfig
import controllers.predicates.AuthPredicate
import services.EnrolmentsAuthService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

@Singleton
class HelloWorldController @Inject()(val enrolmentsAuthService: EnrolmentsAuthService,
                                     val authenticate: AuthPredicate,
                                     val messagesApi: MessagesApi,
                                     implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def helloWorld: Action[AnyContent] = authenticate.async { implicit user =>
    Future.successful(Ok(views.html.hello_world()))
  }
}
