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

import config.AppConfig
import controllers.predicates.AuthPredicate
import javax.inject.{Inject, Singleton}
import play.api.i18n.MessagesApi
import play.api.mvc._

@Singleton
class EmailChangeSuccessController @Inject()(val authenticate: AuthPredicate,
                                             val messagesApi: MessagesApi,
                                             implicit val appConfig: AppConfig) extends BaseController {

  def show: Action[AnyContent] = authenticate { implicit user =>
    Ok(views.html.email_change_success())
  }
}
