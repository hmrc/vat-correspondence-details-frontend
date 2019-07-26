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

package helpers

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.data.Form
import play.api.http.HeaderNames
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.{Application, Environment, Mode}
import stubs.AuthStub

trait IntegrationBaseSpec extends CustomMatchers with GuiceOneServerPerSuite with WireMockHelper with BeforeAndAfterAll {

  val mockHost: String = WireMockHelper.host
  val mockPort: String = WireMockHelper.wireMockPort.toString
  val appRouteContext: String = "/vat-through-software/account/correspondence"

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = MessagesImpl(Lang("en-GB"), messagesApi)

  lazy val client: WSClient = app.injector.instanceOf[WSClient]

  def servicesConfig: Map[String, String] = Map(
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "microservice.services.auth.host" -> mockHost,
    "microservice.services.auth.port" -> mockPort,
    "microservice.services.email-verification.host" -> mockHost,
    "microservice.services.email-verification.port" -> mockPort,
    "microservice.services.vat-subscription.host" -> mockHost,
    "microservice.services.vat-subscription.port" -> mockPort,
    "microservice.services.contact-preferences.host" -> mockHost,
    "microservice.services.contact-preferences.port" -> mockPort,
    "microservice.services.vat-subscription-dynamic-stub.host" -> mockHost,
    "microservice.services.vat-subscription-dynamic-stub.port" -> mockPort,
    "features.emailVerification.enabled" -> "true"
  )

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(servicesConfig)
    .build()

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWireMock()
  }

  override def afterAll(): Unit = {
    stopWireMock()
    super.afterAll()
  }

  class PreconditionBuilder {
    implicit val builder: PreconditionBuilder = this

    def user: User = new User()
  }

  def given: PreconditionBuilder = new PreconditionBuilder

  class User()(implicit builder: PreconditionBuilder) {
    def isAuthenticated: PreconditionBuilder = {
      Given("I stub a User who is successfully signed up to MTD VAT")
      AuthStub.authorised()
      builder
    }

    def isAuthenticatedAgent: PreconditionBuilder = {
      Given("I stub an Agent who is successfully signed up to change their client's MTD VAT details")
      AuthStub.authorisedAgent()
      builder
    }

    def isNotAuthenticated: PreconditionBuilder = {
      Given("I stub a User who is not logged in")
      AuthStub.unauthorisedNotLoggedIn()
      builder
    }

    def isNotEnrolled: PreconditionBuilder = {
      Given("I stub a User who is NOT signed up to MTD VAT")
      AuthStub.unauthorisedOtherEnrolment()
      builder
    }

    def noAffinityGroup: PreconditionBuilder = {
      Given("I stub a User who is authenticated but does NOT have an Affinity Group")
      AuthStub.authorisedNoAffinityGroup()
      builder
    }
  }

  def buildRequest(path: String): WSRequest = client.url(s"http://localhost:$port$appRouteContext$path").withFollowRedirects(false)

  def document(response: WSResponse): Document = Jsoup.parse(response.body)

  def get(path: String, additionalCookies: Map[String, String] = Map.empty): WSResponse = await(
    buildRequest(path, additionalCookies).get()
  )

  def post(path: String, additionalCookies: Map[String, String] = Map.empty)(body: Map[String, Seq[String]]): WSResponse = await(
    buildRequest(path, additionalCookies).post(body)
  )

  def toFormData[T](form: Form[T], data: T): Map[String, Seq[String]] =
    form.fill(data).data map { case (k, v) => k -> Seq(v) }

  def buildRequest(path: String, additionalCookies: Map[String, String] = Map.empty): WSRequest =
    client.url(s"http://localhost:$port$appRouteContext$path")
      .withHttpHeaders(HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(additionalCookies), "Csrf-Token" -> "nocheck")
      .withFollowRedirects(false)
}
