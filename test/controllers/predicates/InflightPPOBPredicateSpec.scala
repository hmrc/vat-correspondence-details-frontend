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

package controllers.predicates

import assets.CustomerInfoConstants._
import common.SessionKeys.inflightPPOBKey
import connectors.httpParsers.GetCustomerInfoHttpParser.{GetCustomerInfoError, GetCustomerInfoResponse}
import mocks.MockAuth
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import play.api.http.Status
import play.api.mvc.{Action, AnyContent}
import play.api.mvc.Results.Ok
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class InflightPPOBPredicateSpec extends MockAuth {

  def setup(result: GetCustomerInfoResponse): Unit = {
    when(mockVatSubscriptionService.getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(result))
  }

  def inflightPredicate: InflightPPOBPredicate =
    new InflightPPOBPredicate(
      mockVatSubscriptionService,
      mockEnrolmentsAuthService,
      mockErrorHandler,
      messagesApi,
      mockConfig
    )

  def target(result: GetCustomerInfoResponse = Right(customerInfoPendingAddressModel)): Action[AnyContent] = {
    setup(result)
    mockIndividualAuthorised()
    inflightPredicate.async {
      implicit user => Future.successful(Ok("test"))
    }
  }

  "The InflightPPOBPredicate" when {

    "there is an inflight indicator in session" when {

      "the inflight indicator is set to 'true'" should {

        lazy val result = await(target()(request.withSession(inflightPPOBKey -> "true")))
        lazy val document = Jsoup.parse(bodyOf(result))

        "return 200" in {
          status(result) shouldBe Status.OK
        }

        "show the 'PPOB change pending' error page" in {
          document.title shouldBe "We are reviewing your request"
        }

        "not call the VatSubscriptionService" in {
          verify(mockVatSubscriptionService, never())
            .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
        }
      }

      "the inflight indicator is set to 'false'" should {

        lazy val result = await(target()(request.withSession(inflightPPOBKey -> "false")))

        "return 200" in {
          status(result) shouldBe Status.OK
        }

        "not call the VatSubscriptionService" in {
          verify(mockVatSubscriptionService, never())
            .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
        }
      }

      "the inflight indicator is set to 'error'" should {

        lazy val result = await(target()(request.withSession(inflightPPOBKey -> "error")))

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "not call the VatSubscriptionService" in {
          verify(mockVatSubscriptionService, never())
            .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
        }
      }
    }

    "there is no inflight indicator in session" when {

      "the user has an inflight PPOB address" should {

        lazy val result = await(target()(request))
        lazy val document = Jsoup.parse(bodyOf(result))

        "return 200" in {
          status(result) shouldBe Status.OK
        }

        "add the inflight indicator 'true' to session" in {
          session(result).get(inflightPPOBKey) shouldBe Some("true")
        }

        "show the 'PPOB change pending' error page" in {
          document.title shouldBe "We are reviewing your request"
        }
      }

      "the user has an inflight email" should {

        lazy val result = await(target(Right(customerInfoPendingEmailModel))(request))
        lazy val document = Jsoup.parse(bodyOf(result))

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

        "add the inflight indicator 'error' to session" in {
          session(result).get(inflightPPOBKey) shouldBe Some("error")
        }

        "show the standard error page" in {
          document.title shouldBe "Sorry, we are experiencing technical difficulties - 500"
        }
      }

      "the user has no inflight information" should {

        lazy val result = await(target(Right(minCustomerInfoModel))(request))

        "return 200" in {
          status(result) shouldBe Status.OK
        }

        "add the inflight indicator 'false' to session" in {
          session(result).get(inflightPPOBKey) shouldBe Some("false")
        }
      }

      "the service call fails" should {

        lazy val result = await(target(Left(GetCustomerInfoError(Status.INTERNAL_SERVER_ERROR, "error")))(request))

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}
