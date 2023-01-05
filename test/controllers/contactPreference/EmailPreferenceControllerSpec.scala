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

package controllers.contactPreference

import common.SessionKeys
import controllers.ControllerBaseSpec
import forms.YesNoForm.{yes, yesNo, no => _no}
import models.contactPreferences.ContactPreference.paper
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.Helpers._
import views.html.contactPreference.EmailPreferenceView
import assets.BaseTestConstants._
import assets.CustomerInfoConstants.{fullCustomerInfoModel, minCustomerInfoModel}
import models.errors.ErrorModel
import play.api.http.Status

class EmailPreferenceControllerSpec extends ControllerBaseSpec {

  lazy val controller = new EmailPreferenceController(mockVatSubscriptionService,
                                                      mockErrorHandler,
                                                      inject[EmailPreferenceView])

  ".show is called" should {

    lazy val result = {
      controller.show(getRequestWithPaperPref.withSession(SessionKeys.contactPrefUpdate -> "true"))
    }

    "return an OK result" in {
      status(result) shouldBe OK
    }

    "return HTML" in {
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    s"not contain the session key ${SessionKeys.contactPrefUpdate}" in {
      session(result).get(SessionKeys.contactPrefUpdate) shouldBe None
    }

    "add the current contact preference to session" in {
      session(result).get(SessionKeys.currentContactPrefKey) shouldBe Some(paper)
    }
  }

  ".submit is called with a Yes and client has an email address" should {

    lazy val result = {
      mockGetCustomerInfo(vrn)(Right(fullCustomerInfoModel))
      controller.submit(postRequestWithPaperPref.withFormUrlEncodedBody(yesNo -> yes))
    }

    "return a SEE_OTHER result" in {
      status(result) shouldBe SEE_OTHER
    }

    "be at the correct url" in {
      redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/preference-confirm-email")
    }

    s"a value is added to the ${SessionKeys.contactPrefUpdate} key" in {
      session(result).get(SessionKeys.contactPrefUpdate) shouldBe Some("true")
    }
  }

  ".submit is called with a Yes and client does not have an email address" should {

    lazy val result = {
      mockGetCustomerInfo(vrn)(Right(minCustomerInfoModel))
      controller.submit(postRequestWithPaperPref.withFormUrlEncodedBody(yesNo -> yes))
    }

    "return a SEE_OTHER result" in {
      status(result) shouldBe SEE_OTHER
    }

    "be at the correct url" in {
      redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/contact-preference/add-email-address")
    }

    s"a value is added to the ${SessionKeys.contactPrefUpdate} key" in {
      session(result).get(SessionKeys.contactPrefUpdate) shouldBe Some("true")
    }
  }

  ".submit is called with but the get customer info call fails" should {

    lazy val result = {
      mockGetCustomerInfo(vrn)(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "")))
      controller.submit(postRequestWithPaperPref.withFormUrlEncodedBody(yesNo -> yes))

    }

    "return an INTERNAL_SERVER_ERROR result" in {
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  ".submit is called with a No" should {

    lazy val result = {
      controller.submit(postRequestWithPaperPref.withFormUrlEncodedBody(yesNo -> _no))
    }

    "return a SEE_OTHER result" in {
      status(result) shouldBe SEE_OTHER
    }

    "be at the correct url" in {
      redirectLocation(result) shouldBe Some(mockConfig.dynamicJourneyEntryUrl(false))
    }
  }

  ".submit is called with no form data" should {

    "return a BAD_REQUEST result" in {

      lazy val result = {
        controller.submit(postRequestWithPaperPref.withFormUrlEncodedBody())
      }

      status(result) shouldBe BAD_REQUEST
    }
  }

  insolvencyCheck(controller.submit())
}
