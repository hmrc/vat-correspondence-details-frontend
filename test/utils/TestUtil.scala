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

package utils

import scala.concurrent.ExecutionContext
import common.SessionKeys._
import config.ErrorHandler
import mocks.MockAppConfig
import models.contactPreferences.ContactPreference.{digital, paper}
import models.User
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.Injector
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.errors.StandardErrorView
import assets.BaseTestConstants._
import org.scalatest.wordspec.AnyWordSpecLike

trait TestUtil extends AnyWordSpecLike with GuiceOneAppPerSuite with MaterializerSupport with BeforeAndAfterEach with Injecting {

  lazy val injector: Injector = app.injector
  implicit lazy val mcc: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]
  implicit lazy val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = MessagesImpl(Lang("en-GB"), messagesApi)
  implicit lazy val mockConfig: MockAppConfig = new MockAppConfig(app.configuration)

  lazy val mockErrorHandler: ErrorHandler = new ErrorHandler(messagesApi, injector.instanceOf[StandardErrorView], mockConfig)

  val testEmail = "test@email.co.uk"
  val testWebsite = "https://www.test-website.co.uk"

  implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(
    inFlightContactDetailsChangeKey -> "false",
    insolventWithoutAccessKey -> "false"
  )
  lazy val requestWithEmail: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(prepopulationEmailKey -> testEmail)

  lazy val requestWithValidationEmail: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(validationEmailKey -> testEmail)

  lazy val requestWithWebsite: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(validationWebsiteKey -> testWebsite, prepopulationWebsiteKey -> testWebsite)

  lazy val requestWithValidationPhoneNumbers: FakeRequest[AnyContentAsEmpty.type] = request.withSession(
    validationLandlineKey -> testValidationLandline,
    validationMobileKey -> testValidationMobile
  )

  lazy val requestWithValidationLandlineNumber: FakeRequest[AnyContentAsEmpty.type] = request.withSession(
    validationLandlineKey -> testValidationLandline
  )

  lazy val requestWithValidationMobileNumber: FakeRequest[AnyContentAsEmpty.type] = request.withSession(
    validationMobileKey -> testValidationMobile
  )

  lazy val requestWithPrepopLandlineNumber: FakeRequest[AnyContentAsEmpty.type] = request.withSession(
    prepopulationLandlineKey -> testPrepopLandline
  )

  lazy val requestWithPrepopMobileNumber: FakeRequest[AnyContentAsEmpty.type] = request.withSession(
    prepopulationMobileKey -> testPrepopMobile
  )

  lazy val fakeRequestWithClientsVRN: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(mtdVatvcClientVrn -> vrn)

  lazy val fakeRequestWithSessionKeys: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(
      mtdVatvcClientVrn -> vrn,
      validationMobileKey -> testValidationMobile,
      validationLandlineKey -> testValidationLandline,
      validationWebsiteKey -> testWebsite
    )

  lazy val requestWithAllLandlineNumbers: FakeRequest[AnyContentAsEmpty.type] = request.withSession(
    validationLandlineKey -> testValidationLandline,
    prepopulationLandlineKey -> testPrepopLandline
  )

  lazy val requestWithAllMobileNumbers: FakeRequest[AnyContentAsEmpty.type] = request.withSession(
    validationMobileKey -> testValidationMobile,
    prepopulationMobileKey -> testPrepopMobile
  )

  lazy val requestWithDigitalPref: FakeRequest[AnyContentAsEmpty.type] = request.withSession(
    currentContactPrefKey -> digital
  )

  lazy val requestWithPaperPref: FakeRequest[AnyContentAsEmpty.type] = request.withSession(
    currentContactPrefKey -> paper
  )

  lazy val requestWithBadFormAndEmail = requestWithValidationEmail
    .withFormUrlEncodedBody("" -> "")

  lazy val insolventRequest: FakeRequest[AnyContentAsEmpty.type] = request.withSession(
    insolventWithoutAccessKey -> "true"
  )

  lazy val user: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn, active = true)(request)
  lazy val userWithValidationEmail: User[AnyContentAsEmpty.type] =
    User[AnyContentAsEmpty.type](vrn, active = true)(request.withSession(validationEmailKey -> "old.email@email.com"))
  lazy val userWithEmptyValidationEmail: User[AnyContentAsEmpty.type] =
    User[AnyContentAsEmpty.type](vrn, active = true)(request.withSession(validationEmailKey -> ""))
  lazy val agent: User[AnyContentAsEmpty.type] =
    User[AnyContentAsEmpty.type](vrn, active = true, Some(arn))(fakeRequestWithClientsVRN)

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit lazy val ec: ExecutionContext = mcc.executionContext
}