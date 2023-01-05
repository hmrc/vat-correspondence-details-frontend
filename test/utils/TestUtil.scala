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
import play.api.test.Helpers.CONTENT_TYPE

trait TestUtil extends AnyWordSpecLike with GuiceOneAppPerSuite with MaterializerSupport with BeforeAndAfterEach with Injecting {

  lazy val injector: Injector = app.injector
  implicit lazy val mcc: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]
  implicit lazy val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = MessagesImpl(Lang("en-GB"), messagesApi)
  implicit lazy val mockConfig: MockAppConfig = new MockAppConfig(app.configuration)

  lazy val mockErrorHandler: ErrorHandler = new ErrorHandler(messagesApi, injector.instanceOf[StandardErrorView], mockConfig)

  val testEmail = "test@email.co.uk"
  val testWebsite = "https://www.test-website.co.uk"

  implicit lazy val getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/path").withSession(
    inFlightContactDetailsChangeKey -> "false",
    insolventWithoutAccessKey -> "false"
  )

  implicit lazy val postRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "/path")
    .withHeaders(CONTENT_TYPE -> "application/x-www-form-urlencoded")
    .withSession(
    inFlightContactDetailsChangeKey -> "false",
    insolventWithoutAccessKey -> "false"
  )

  lazy val getRequestWithEmail: FakeRequest[AnyContentAsEmpty.type] =
    getRequest.withSession(prepopulationEmailKey -> testEmail)

  lazy val postRequestWithEmail: FakeRequest[AnyContentAsEmpty.type] =
    postRequest.withSession(prepopulationEmailKey -> testEmail)

  lazy val postRequestWithValidationEmail: FakeRequest[AnyContentAsEmpty.type] =
    postRequest.withSession(validationEmailKey -> testEmail)

  lazy val getRequestWithWebsite: FakeRequest[AnyContentAsEmpty.type] =
    getRequest.withSession(validationWebsiteKey -> testWebsite, prepopulationWebsiteKey -> testWebsite)

  lazy val postRequestWithWebsite: FakeRequest[AnyContentAsEmpty.type] =
    postRequest.withSession(validationWebsiteKey -> testWebsite, prepopulationWebsiteKey -> testWebsite)

  lazy val requestWithValidationPhoneNumbers: FakeRequest[AnyContentAsEmpty.type] = getRequest.withSession(
    validationLandlineKey -> testValidationLandline,
    validationMobileKey -> testValidationMobile
  )

  lazy val getRequestWithValidationLandlineNumber: FakeRequest[AnyContentAsEmpty.type] = getRequest.withSession(
    validationLandlineKey -> testValidationLandline
  )

  lazy val postRequestWithValidationLandlineNumber: FakeRequest[AnyContentAsEmpty.type] = postRequest.withSession(
    validationLandlineKey -> testValidationLandline
  )

  lazy val getRequestWithValidationMobileNumber: FakeRequest[AnyContentAsEmpty.type] = getRequest.withSession(
    validationMobileKey -> testValidationMobile
  )

  lazy val postRequestWithValidationMobileNumber: FakeRequest[AnyContentAsEmpty.type] = postRequest.withSession(
    validationMobileKey -> testValidationMobile
  )

  lazy val getRequestWithPrepopLandlineNumber: FakeRequest[AnyContentAsEmpty.type] = getRequest.withSession(
    prepopulationLandlineKey -> testPrepopLandline
  )

  lazy val postRequestWithPrepopLandlineNumber: FakeRequest[AnyContentAsEmpty.type] = postRequest.withSession(
    prepopulationLandlineKey -> testPrepopLandline
  )

  lazy val getRequestWithPrepopMobileNumber: FakeRequest[AnyContentAsEmpty.type] = getRequest.withSession(
    prepopulationMobileKey -> testPrepopMobile
  )

  lazy val postRequestWithPrepopMobileNumber: FakeRequest[AnyContentAsEmpty.type] = postRequest.withSession(
    prepopulationMobileKey -> testPrepopMobile
  )

  lazy val fakeRequestWithClientsVRN: FakeRequest[AnyContentAsEmpty.type] =
    getRequest.withSession(mtdVatvcClientVrn -> vrn)

  lazy val fakeGetRequestWithSessionKeys: FakeRequest[AnyContentAsEmpty.type] =
    getRequest.withSession(
      mtdVatvcClientVrn -> vrn,
      validationMobileKey -> testValidationMobile,
      validationLandlineKey -> testValidationLandline,
      validationWebsiteKey -> testWebsite
    )

  lazy val getRequestWithAllLandlineNumbers: FakeRequest[AnyContentAsEmpty.type] = getRequest.withSession(
    validationLandlineKey -> testValidationLandline,
    prepopulationLandlineKey -> testPrepopLandline
  )

  lazy val postRequestWithAllLandlineNumbers: FakeRequest[AnyContentAsEmpty.type] = postRequest.withSession(
    validationLandlineKey -> testValidationLandline,
    prepopulationLandlineKey -> testPrepopLandline
  )

  lazy val getRequestWithAllMobileNumbers: FakeRequest[AnyContentAsEmpty.type] = getRequest.withSession(
    validationMobileKey -> testValidationMobile,
    prepopulationMobileKey -> testPrepopMobile
  )

  lazy val postRequestWithAllMobileNumbers: FakeRequest[AnyContentAsEmpty.type] = postRequest.withSession(
    validationMobileKey -> testValidationMobile,
    prepopulationMobileKey -> testPrepopMobile
  )

  lazy val getRequestWithDigitalPref: FakeRequest[AnyContentAsEmpty.type] = getRequest.withSession(
    currentContactPrefKey -> digital
  )

  lazy val getRequestWithPaperPref: FakeRequest[AnyContentAsEmpty.type] = getRequest.withSession(
    currentContactPrefKey -> paper
  )

  lazy val postRequestWithPaperPref: FakeRequest[AnyContentAsEmpty.type] = postRequest.withSession(
    currentContactPrefKey -> paper
  )

  lazy val postRequestWithBadFormAndEmail = postRequestWithValidationEmail
    .withFormUrlEncodedBody("" -> "")

  lazy val insolventRequest: FakeRequest[AnyContentAsEmpty.type] = getRequest.withSession(
    insolventWithoutAccessKey -> "true"
  )

  lazy val user: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn, active = true)(getRequest)
  lazy val userWithValidationEmail: User[AnyContentAsEmpty.type] =
    User[AnyContentAsEmpty.type](vrn, active = true)(getRequest.withSession(validationEmailKey -> "old.email@email.com"))
  lazy val userWithEmptyValidationEmail: User[AnyContentAsEmpty.type] =
    User[AnyContentAsEmpty.type](vrn, active = true)(getRequest.withSession(validationEmailKey -> ""))
  lazy val agent: User[AnyContentAsEmpty.type] =
    User[AnyContentAsEmpty.type](vrn, active = true, Some(arn))(fakeRequestWithClientsVRN)

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit lazy val ec: ExecutionContext = mcc.executionContext
}