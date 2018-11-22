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

import config.AppConfig
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.language.LanguageUtils

@Singleton
class LanguageController @Inject()(val appConfig: AppConfig, implicit val messagesApi: MessagesApi) extends BaseController {

  def languageMap: Map[String, Lang] = appConfig.languageMap

  def switchToLanguage(language: String): Action[AnyContent] = Action { implicit request =>
    val lang = languageMap.getOrElse(language, LanguageUtils.getCurrentLang)
    val redirectURL = request.headers.get(REFERER).getOrElse(appConfig.manageVatSubscriptionServicePath)

    Redirect(redirectURL).withLang(Lang.apply(lang.code)).flashing(LanguageUtils.FlashWithSwitchIndicator)
  }
}
