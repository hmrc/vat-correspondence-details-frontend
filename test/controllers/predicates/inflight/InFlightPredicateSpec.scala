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

package controllers.predicates.inflight

import assets.BaseTestConstants.internalServerErrorTitle
import assets.CustomerInfoConstants._
import common.SessionKeys.inFlightContactDetailsChangeKey
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import mocks.MockAuth
import models.User
import models.customerInformation.PendingChanges
import models.errors.ErrorModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class InFlightPredicateSpec extends MockAuth {

  def setup(result: GetCustomerInfoResponse = Right(customerInfoPendingAddressModel)): Unit =
    when(mockVatSubscriptionService.getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(result))

  val inflightPPOBPredicate = new InFlightPredicate(
    mockInFlightPredicateComponents,
    "/redirect-location"
  )

  def userWithSession(inflightPPOBValue: String): User[AnyContentAsEmpty.type] =
    User[AnyContentAsEmpty.type]("999943620")(request.withSession(inFlightContactDetailsChangeKey -> inflightPPOBValue))

  val userWithoutSession: User[AnyContentAsEmpty.type] = User("999999999")(FakeRequest())

  "The InFlightPredicate" when {

    "there is an inflight indicator in session" when {

      "the inflight indicator is set to a change value" should {

        lazy val result = await(inflightPPOBPredicate.refine(userWithSession("ppob"))).left.get
        lazy val document = Jsoup.parse(bodyOf(result))

        "return 409" in {
          status(result) shouldBe Status.CONFLICT
        }

        "show the 'change pending' error page" in {
          messages(document.title) shouldBe "There is already a change pending - Business tax account - GOV.UK"
        }

        "not call the VatSubscriptionService" in {
          verify(mockVatSubscriptionService, never())
            .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
        }
      }

      "the inflight indicator is set to 'false'" should {

        lazy val result = await(inflightPPOBPredicate.refine(userWithSession("false")))

        "allow the request to pass through the predicate" in {
          result shouldBe Right(userWithSession("false"))
        }

        "not call the VatSubscriptionService" in {
          verify(mockVatSubscriptionService, never())
            .getCustomerInfo(any[String])(any[HeaderCarrier], any[ExecutionContext])
        }
      }
    }

    "there is no inflight indicator in session" when {

      "the user has an inflight PPOB address" should {

        lazy val result = {
          setup()
          await(inflightPPOBPredicate.refine(userWithoutSession)).left.get
        }
        lazy val document = Jsoup.parse(bodyOf(result))

        "return 409" in {
          status(result) shouldBe Status.CONFLICT
        }

        "add the inflight indicator 'ppob' to session" in {
          session(result).get(inFlightContactDetailsChangeKey) shouldBe Some("ppob")
        }

        "show the 'change pending' error page" in {
          messages(document.title) shouldBe "There is already a change pending - Business tax account - GOV.UK"
        }
      }

      "the user has an inflight email" should {

        lazy val result = {
          setup(Right(customerInfoPendingEmailModel))
          await(inflightPPOBPredicate.refine(userWithoutSession)).left.get
        }
        lazy val document = Jsoup.parse(bodyOf(result))

        "return 409" in {
          status(result) shouldBe Status.CONFLICT
        }

        "add the inflight indicator 'email' to session" in {
          session(result).get(inFlightContactDetailsChangeKey) shouldBe Some("email")
        }

        "show the 'change pending' error page" in {
          messages(document.title) shouldBe "There is already a change pending - Business tax account - GOV.UK"
        }
      }

      "the user has an inflight landline number" should {

        lazy val result = {
          setup(Right(customerInfoPendingLandlineModel))
          await(inflightPPOBPredicate.refine(userWithoutSession)).left.get
        }
        lazy val document = Jsoup.parse(bodyOf(result))

        "return 409" in {
          status(result) shouldBe Status.CONFLICT
        }

        "add the inflight indicator 'landline' to session" in {
          session(result).get(inFlightContactDetailsChangeKey) shouldBe Some("landline")
        }

        "show the 'change pending' error page" in {
          messages(document.title) shouldBe "There is already a change pending - Business tax account - GOV.UK"
        }
      }

      "the user has an inflight mobile number" should {

        lazy val result = {
          setup(Right(customerInfoPendingMobileModel))
          await(inflightPPOBPredicate.refine(userWithoutSession)).left.get
        }
        lazy val document = Jsoup.parse(bodyOf(result))

        "return 409" in {
          status(result) shouldBe Status.CONFLICT
        }

        "add the inflight indicator 'mobile' to session" in {
          session(result).get(inFlightContactDetailsChangeKey) shouldBe Some("mobile")
        }

        "show the 'change pending' error page" in {
          messages(document.title) shouldBe "There is already a change pending - Business tax account - GOV.UK"
        }
      }

      "the user has an inflight website address" should {

        lazy val result = {
          setup(Right(customerInfoPendingWebsiteModel))
          await(inflightPPOBPredicate.refine(userWithoutSession)).left.get
        }
        lazy val document = Jsoup.parse(bodyOf(result))

        "return 409" in {
          status(result) shouldBe Status.CONFLICT
        }

        "add the inflight indicator 'website' to session" in {
          session(result).get(inFlightContactDetailsChangeKey) shouldBe Some("website")
        }

        "show the 'change pending' error page" in {
          messages(document.title) shouldBe "There is already a change pending - Business tax account - GOV.UK"
        }
      }

      "the user has no inflight information" should {

        lazy val result = {
          setup(Right(minCustomerInfoModel))
          await(inflightPPOBPredicate.refine(userWithoutSession)).left.get
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect the user to the predicate's redirect URL" in {
          redirectLocation(result) shouldBe Some("/redirect-location")
        }

        "add the inflight indicator 'false' to session" in {
          session(result).get(inFlightContactDetailsChangeKey) shouldBe Some("false")
        }
      }

      "the user has a non-PPOB change pending" should {

        lazy val result = {
          setup(Right(minCustomerInfoModel.copy(pendingChanges = Some(PendingChanges(None)))))
          await(inflightPPOBPredicate.refine(userWithoutSession)).left.get
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect the user to the capture email address page" in {
          redirectLocation(result) shouldBe Some("/redirect-location")
        }

        "add the inflight indicator 'false' to session" in {
          session(result).get(inFlightContactDetailsChangeKey) shouldBe Some("false")
        }
      }

      "the service call fails" should {

        lazy val result = {
          setup(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "error")))
          await(inflightPPOBPredicate.refine(userWithoutSession)).left.get
        }

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}
