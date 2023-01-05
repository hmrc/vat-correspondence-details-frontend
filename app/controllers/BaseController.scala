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

package controllers

import controllers.predicates.contactPreference.{ContactPrefPredicate, ContactPrefPredicateComponents}
import controllers.predicates.inflight.{InFlightPredicate, InFlightPredicateComponents}
import controllers.predicates.{AuthPredicate, AuthPredicateComponents}
import models.contactPreferences.ContactPreference.{digital, paper}
import play.api.i18n.I18nSupport
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController


abstract class BaseController(implicit authComps: AuthPredicateComponents,
                              inFlightComps: InFlightPredicateComponents) extends FrontendController(authComps.mcc) with I18nSupport {

  val allowAgentPredicate: AuthPredicate = new AuthPredicate(authComps, allowsAgents = true)
  val blockAgentPredicate = new AuthPredicate(authComps, allowsAgents = false)
  val contactPreferencePredicate = new AuthPredicate(authComps, allowsAgents = false, isChangePrefJourney = true)

  val routePrefix = "/vat-through-software/account/correspondence"

  val inFlightEmailPredicate = new InFlightPredicate(
    inFlightComps,
    routePrefix + controllers.email.routes.CaptureEmailController.show.url,
    blockIfPendingPref = false
  )
  val inFlightWebsitePredicate = new InFlightPredicate(
    inFlightComps,
    routePrefix + controllers.website.routes.CaptureWebsiteController.show.url,
    blockIfPendingPref = false
  )
  val inFlightLandlineNumberPredicate = new InFlightPredicate(
    inFlightComps,
    routePrefix + controllers.landlineNumber.routes.CaptureLandlineNumberController.show.url,
    blockIfPendingPref = false
  )
  val inFlightMobileNumberPredicate = new InFlightPredicate(
    inFlightComps,
    routePrefix + controllers.mobileNumber.routes.CaptureMobileNumberController.show.url,
    blockIfPendingPref = false
  )

  val inFlightContactPrefPredicate = new InFlightPredicate(
    inFlightComps,
    routePrefix + controllers.contactPreference.routes.ContactPreferenceRedirectController.redirect.url,
    blockIfPendingPref = true
  )

  val contactPrefComps = new ContactPrefPredicateComponents(
    inFlightComps.vatSubscriptionService,
    authComps.errorHandler,
    authComps.mcc,
    authComps.messagesApi,
    authComps.appConfig
  )

  val digitalPrefPredicate = new ContactPrefPredicate(contactPrefComps, blockedPref = paper)
  val paperPrefPredicate = new ContactPrefPredicate(contactPrefComps, blockedPref = digital)
}
