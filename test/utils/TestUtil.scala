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

package utils

import assets.BaseTestConstants._
import common.SessionKeys
import config.ErrorHandler
import mocks.MockAppConfig
import models.User
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.Injector
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.filters.csrf.CSRF.Token
import play.filters.csrf.{CSRFConfigProvider, CSRFFilter}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext

trait TestUtil extends UnitSpec with GuiceOneAppPerSuite with BeforeAndAfterEach with MaterializerSupport{

  lazy val injector: Injector = app.injector
  lazy val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = Messages(Lang("en-GB"), messagesApi)

  implicit lazy val mockConfig: MockAppConfig = new MockAppConfig(app.configuration)
  implicit lazy val serviceErrorHandler: ErrorHandler = injector.instanceOf[ErrorHandler]

  lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  lazy val fakeRequestWithVrnAndRedirectUrl: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withSession(
      SessionKeys.clientVrn -> vrn,
      SessionKeys.redirectUrl -> "/homepage"
    )

  implicit lazy val fakeRequestWithClientsVRN: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withSession(SessionKeys.clientVrn -> vrn)

  lazy val user: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn, active = true)(request)
  lazy val agent: User[AnyContentAsEmpty.type] =
    User[AnyContentAsEmpty.type](vrn, active = true, Some(arn))(fakeRequestWithVrnAndRedirectUrl)

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit lazy val ec: ExecutionContext = injector.instanceOf[ExecutionContext]

  implicit class CSRFTokenAdder[T](req: FakeRequest[T]) {

    def addToken(): FakeRequest[T] = {
      val csrfConfig = app.injector.instanceOf[CSRFConfigProvider].get
      val csrfFilter = app.injector.instanceOf[CSRFFilter]
      val token = csrfFilter.tokenProvider.generateToken

      req.copyFakeRequest(tags = req.tags ++ Map(
        Token.NameRequestTag -> csrfConfig.tokenName,
        Token.RequestTag -> token
      )).withHeaders(csrfConfig.headerName -> token)
    }
  }
}

