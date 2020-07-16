/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.contactPreference

import config.AppConfig
import controllers.BaseController
import controllers.predicates.AuthPredicateComponents
import controllers.predicates.inflight.InFlightPredicateComponents
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent}

class ChangeEmailController @Inject()(implicit val appConfig: AppConfig,
                                      authComps: AuthPredicateComponents,
                                      inFlightPredicateComponents: InFlightPredicateComponents) extends BaseController {

  //TODO: Update as part of BTAT-8061
  def show: Action[AnyContent] = (contactPreferencePredicate andThen paperPrefPredicate) { implicit user =>
    if(appConfig.features.letterToConfirmedEmailEnabled()) {
      Ok
    } else {
      NotFound(authComps.errorHandler.notFoundTemplate)
    }
  }
}
