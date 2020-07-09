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

package controllers.predicates.contactPreferences

import assets.CustomerInfoConstants.{customerInfoPaperPrefModel, fullCustomerInfoModel, minCustomerInfoModel}
import common.SessionKeys.currentContactPrefKey
import controllers.predicates.contactPreference.{ContactPrefPredicate, ContactPrefPredicateComponents}
import mocks.MockAuth
import models.User
import models.contactPreferences.ContactPreference.{digital, paper}
import models.errors.ErrorModel
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.Helpers._

class ContactPrefPredicateSpec extends MockAuth {

  val predicateComponents = new ContactPrefPredicateComponents(
    mockVatSubscriptionService, mockErrorHandler, mcc, messagesApi, mockConfig
  )

  val digitalPrefPredicate = new ContactPrefPredicate(predicateComponents, blockedPref = paper)
  val paperPrefPredicate = new ContactPrefPredicate(predicateComponents, blockedPref = digital)

  val digitalUser: User[AnyContentAsEmpty.type] = User("999999999")(requestWithDigitalPref)
  val paperUser: User[AnyContentAsEmpty.type] = User("999999999")(requestWithPaperPref)

  "The ContactPrefPredicate" when {

    "there is a current contact preference in session" when {

      "the session value is 'digital'" when {

        "this preference is blocked" should {

          lazy val result = await(paperPrefPredicate.refine(digitalUser)).left.get

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect the user to BTA" in {
            redirectLocation(result) shouldBe Some(mockConfig.btaAccountDetailsUrl)
          }
        }

        "this preference is allowed" should {

          lazy val result = await(digitalPrefPredicate.refine(digitalUser)).right.get

          "let the request pass through" in {
            result shouldBe digitalUser
          }
        }
      }

      "the session value is 'paper'" when {

        "this preference is blocked" should {

          lazy val result = await(digitalPrefPredicate.refine(paperUser)).left.get

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect the user to BTA" in {
            redirectLocation(result) shouldBe Some(mockConfig.btaAccountDetailsUrl)
          }
        }

        "this preference is allowed" should {

          lazy val result = await(paperPrefPredicate.refine(paperUser)).right.get

          "let the request pass through" in {
            result shouldBe paperUser
          }
        }
      }
    }

    "there is no current contact preference in session" when {

      "the user's preference is 'digital'" when {

        "this preference is blocked" should {

          lazy val result = {
            mockGetCustomerInfo(user.vrn)(Right(fullCustomerInfoModel))
            await(paperPrefPredicate.refine(user)).left.get
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect the user to BTA" in {
            redirectLocation(result) shouldBe Some(mockConfig.btaAccountDetailsUrl)
          }

          "add the preference to the session" in {
            session(result).get(currentContactPrefKey) shouldBe Some(digital)
          }
        }

        "this preference is allowed" should {

          lazy val result = {
            mockGetCustomerInfo(user.vrn)(Right(fullCustomerInfoModel))
            await(digitalPrefPredicate.refine(user)).right.get
          }

          "let the request pass through" in {
            result shouldBe user
          }
        }
      }

      "the user's preference is 'paper'" when {

        "this preference is blocked" should {

          lazy val result = {
            mockGetCustomerInfo(user.vrn)(Right(customerInfoPaperPrefModel))
            await(digitalPrefPredicate.refine(user)).left.get
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect the user to BTA" in {
            redirectLocation(result) shouldBe Some(mockConfig.btaAccountDetailsUrl)
          }

          "add the preference to the session" in {
            session(result).get(currentContactPrefKey) shouldBe Some(paper)
          }
        }

        "this preference is allowed" should {

          lazy val result = {
            mockGetCustomerInfo(user.vrn)(Right(customerInfoPaperPrefModel))
            await(paperPrefPredicate.refine(user)).right.get
          }

          "let the request pass through" in {
            result shouldBe user
          }
        }
      }

      "the user has no preference in ETMP" should {

        lazy val result = {
          mockGetCustomerInfo(user.vrn)(Right(minCustomerInfoModel))
          await(paperPrefPredicate.refine(user)).left.get
        }

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }
      }

      "the call to customer info fails" should {

        lazy val result = {
          mockGetCustomerInfo(user.vrn)(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "error")))
          await(paperPrefPredicate.refine(user)).left.get
        }

        "return 500" in {
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}
