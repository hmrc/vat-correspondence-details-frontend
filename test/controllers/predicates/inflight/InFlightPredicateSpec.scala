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

package controllers.predicates.inflight

import assets.CustomerInfoConstants._
import common.SessionKeys._
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import mocks.MockAuth
import models.User
import models.customerInformation.PendingChanges
import models.errors.ErrorModel
import org.jsoup.Jsoup
import org.scalatest.matchers.should.Matchers
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.mvc.Results.BadRequest
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class InFlightPredicateSpec extends MockAuth with Matchers {

  def setup(result: GetCustomerInfoResponse = Right(customerInfoPendingAddressModel)): Unit =
    mockGetCustomerInfo("999999999")(result)

  val inflightPPOBPredicate = new InFlightPredicate(
    mockInFlightPredicateComponents,
    "/redirect-location",
    blockIfPendingPref = false
  )

  val inflightCommsPrefPredicate = new InFlightPredicate(
    mockInFlightPredicateComponents,
    "/redirect-location",
    blockIfPendingPref = true
  )

  def userWithSession(inflightPPOBValue: String): User[AnyContentAsEmpty.type] =
    User[AnyContentAsEmpty.type]("999943620")(getRequest.withSession(inFlightContactDetailsChangeKey -> inflightPPOBValue))

  val userWithoutSession: User[AnyContentAsEmpty.type] = User("999999999")(FakeRequest())

  "The InFlightPredicate" when {

    "there is an inflight indicator in session" when {

      "the inflight indicator is set to 'commsPref' and the blockIfPendingPref parameter is true" should {

        lazy val result = Future.successful(await(inflightCommsPrefPredicate.refine(userWithSession("commsPref"))).swap.getOrElse(BadRequest))
        lazy val document = Jsoup.parse(contentAsString(result))

        "return 409" in {
          status(result) shouldBe Status.CONFLICT
        }

        "show the 'change pending' error page" in {
          messages(document.title) shouldBe "There is already a change pending - Manage your VAT account - GOV.UK"
        }
      }

      "the inflight indicator is set to 'true'" should {

        lazy val result = Future.successful(await(inflightPPOBPredicate.refine(userWithSession("true"))).swap.getOrElse(BadRequest))
        lazy val document = Jsoup.parse(contentAsString(result))

        "return 409" in {
          status(result) shouldBe Status.CONFLICT
        }

        "show the 'change pending' error page" in {
          messages(document.title) shouldBe "There is already a change pending - Manage your VAT account - GOV.UK"
        }
      }

      "the inflight indicator is set to 'false'" should {

        lazy val result = await(inflightPPOBPredicate.refine(userWithSession("false")))

        "allow the request to pass through the predicate" in {
          result shouldBe Right(userWithSession("false"))
        }
      }
    }

    "there is no inflight indicator in session" when {

      "the user has an inflight PPOB section" should {

        lazy val result = {
          setup()
          Future.successful(await(inflightPPOBPredicate.refine(userWithoutSession)).swap.getOrElse(BadRequest))
        }
        lazy val document = Jsoup.parse(contentAsString(result))

        "return 409" in {
          status(result) shouldBe Status.CONFLICT
        }

        "add the inflight indicator 'true' to session" in {
          session(result).get(inFlightContactDetailsChangeKey) shouldBe Some("true")
        }

        "show the 'change pending' error page" in {
          messages(document.title) shouldBe "There is already a change pending - Manage your VAT account - GOV.UK"
        }
      }

      "the user has an inflight commsPreference" when {

        "the blockIfPendingPref parameter is true" should {

          lazy val result = {
            setup(Right(customerInfoPendingContactPrefModel))
            Future.successful(await(inflightCommsPrefPredicate.refine(userWithoutSession)).swap.getOrElse(BadRequest))
          }
          lazy val document = Jsoup.parse(contentAsString(result))

          "return 409" in {
            status(result) shouldBe Status.CONFLICT
          }

          "show the 'change pending' error page" in {
            messages(document.title) shouldBe "There is already a change pending - Manage your VAT account - GOV.UK"
          }

          "add the inflight indicator 'commsPref' to session" in {
            session(result).get(inFlightContactDetailsChangeKey) shouldBe Some("commsPref")
          }
        }

        "the blockIfPendingPref parameter is false" should {

          lazy val result = {
            setup(Right(customerInfoPendingContactPrefModel))
            Future.successful(await(inflightPPOBPredicate.refine(userWithoutSession)).swap.getOrElse(BadRequest))
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
      }

      "the user has no inflight information" should {

        lazy val result = {
          setup(Right(minCustomerInfoModel.copy(ppob = fullPPOBModel)))
          Future.successful(await(inflightPPOBPredicate.refine(userWithoutSession)).swap.getOrElse(BadRequest))
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

        "add the validationEmailKey to session" in {
          session(result).get(validationEmailKey) shouldBe Some("pepsimac@gmail.com")
        }

        "add the validationLandlineKey to session" in {
          session(result).get(validationLandlineKey) shouldBe Some("01234567890")
        }

        "add the validationMobileKey to session" in {
          session(result).get(validationMobileKey) shouldBe Some("07707707707")
        }

        "add the validationWebsiteKey to session" in {
          session(result).get(validationWebsiteKey) shouldBe Some("www.pepsi-mac.biz")
        }
      }

      "the user has a change pending that the predicate does not cater for" should {

        lazy val result = {
          setup(Right(minCustomerInfoModel.copy(pendingChanges = Some(PendingChanges(None, None)))))
          Future.successful(await(inflightPPOBPredicate.refine(userWithoutSession)).swap.getOrElse(BadRequest))
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

      "the service call fails" should {

        lazy val result = {
          setup(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "error")))
          Future.successful(await(inflightPPOBPredicate.refine(userWithoutSession)).swap.getOrElse(BadRequest))
        }

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}
