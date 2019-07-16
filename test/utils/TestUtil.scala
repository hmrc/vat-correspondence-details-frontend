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

package utils

import scala.concurrent.ExecutionContext
import assets.BaseTestConstants._
import common.SessionKeys
import config.ErrorHandler
import mocks.MockAppConfig
import models.User
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.Injector
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import views.html.errors.StandardErrorView

trait TestUtil extends UnitSpec with GuiceOneAppPerSuite with MaterializerSupport with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    super.beforeEach()
    mockConfig.features.contactPreferencesEnabled(false)
    mockConfig.features.languageSelectorEnabled(true)
    mockConfig.features.changeWebsiteEnabled(true)
  }

  lazy val injector: Injector = app.injector
  lazy val mcc: MessagesControllerComponents = stubMessagesControllerComponents()
  implicit lazy val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = MessagesImpl(Lang("en-GB"), messagesApi)
  implicit lazy val mockConfig: MockAppConfig = new MockAppConfig(app.configuration)

  lazy val mockErrorHandler: ErrorHandler = new ErrorHandler(messagesApi, injector.instanceOf[StandardErrorView], mockConfig)

  val testEmail = "test@email.co.uk"
  val testWebsite = "https://www.test-website.co.uk"

  implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  lazy val requestWithEmail: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(SessionKeys.emailKey -> testEmail)

  lazy val requestWithWebsite: FakeRequest[AnyContentAsEmpty.type] =
    request.withSession(SessionKeys.prepopulationWebsiteKey -> testWebsite)

  lazy val fakeRequestWithClientsVRN: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withSession(SessionKeys.clientVrn -> vrn)

  lazy val user: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn, active = true)(request)
  lazy val agent: User[AnyContentAsEmpty.type] =
    User[AnyContentAsEmpty.type](vrn, active = true, Some(arn))(fakeRequestWithClientsVRN)

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit lazy val ec: ExecutionContext = mcc.executionContext
}
