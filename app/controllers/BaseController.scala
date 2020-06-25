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

package controllers

import controllers.predicates.inflight.{InFlightPredicate, InFlightPredicateComponents}
import controllers.predicates.{AuthPredicate, AuthPredicateComponents}
import play.api.i18n.I18nSupport
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.controller.FrontendController


abstract class BaseController(implicit val mcc: MessagesControllerComponents,
                              authComps: AuthPredicateComponents,
                              inFlightComps: InFlightPredicateComponents) extends FrontendController(mcc) with I18nSupport {

  val allowAgentPredicate = new AuthPredicate(authComps, allowsAgents = true)
  val blockAgentPredicate = new AuthPredicate(authComps, allowsAgents = false)
  val changePrefPredicate = new AuthPredicate(authComps, allowsAgents = false, true)

  val routePrefix = "/vat-through-software/account/correspondence"

  val inFlightEmailPredicate = new InFlightPredicate(
    inFlightComps, routePrefix + controllers.email.routes.CaptureEmailController.show().url
  )
  val inFlightWebsitePredicate = new InFlightPredicate(
    inFlightComps, routePrefix + controllers.website.routes.CaptureWebsiteController.show().url
  )
  val inFlightLandlineNumberPredicate = new InFlightPredicate(
    inFlightComps, routePrefix + controllers.landlineNumber.routes.CaptureLandlineNumberController.show().url
  )
  val inFlightMobileNumberPredicate = new InFlightPredicate(
    inFlightComps, routePrefix + controllers.mobileNumber.routes.CaptureMobileNumberController.show().url
  )
}
