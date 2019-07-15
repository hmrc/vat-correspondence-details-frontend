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

package controllers

import assets.BaseTestConstants._
import models.User
import models.customerInformation.UpdatePPOBSuccess
import models.errors.ErrorModel
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.http.Status.{CONFLICT, INTERNAL_SERVER_ERROR}
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import views.html.ConfirmWebsiteView

import scala.concurrent.Future

class ConfirmWebsiteControllerSpec extends ControllerBaseSpec  {

  object TestConfirmWebsiteController extends ConfirmWebsiteController(
    mockAuthPredicateComponents,
    mcc,
    mockErrorHandler,
    mockVatSubscriptionService,
    injector.instanceOf[ConfirmWebsiteView],
    mockConfig
  )

  "Calling the extractWebsite function in ConfirmWebsiteController" when {

    "there is an authenticated request from a user with a website in session" should {

      "result in a website being retrieved" in {
        mockIndividualAuthorised()
        val user = User[AnyContent](vrn, active = true, None)(requestWithWebsite)

        TestConfirmWebsiteController.extractSessionWebsite(user) shouldBe Some(testWebsite)
      }
    }
  }

  "Calling the show action in ConfirmWebsiteController" when {

    "there is a website in session" should {

      "show the Confirm Website page" in {
        mockIndividualAuthorised()
        val result = TestConfirmWebsiteController.show(requestWithWebsite)

        status(result) shouldBe Status.OK
      }
    }

    "there isn't a website in session" should {

      "take the user to enter a new website" in {
        mockIndividualAuthorised()
        val result = TestConfirmWebsiteController.show(request)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/new-website-address")
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = TestConfirmWebsiteController.show(requestWithWebsite)

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    "the changeWebsite feature is disabled" should {

      "present the server error page" in {
        mockConfig.features.changeWebsiteEnabled(false)
        mockIndividualAuthorised()

        val result = TestConfirmWebsiteController.show(request)

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        mockConfig.features.changeWebsiteEnabled(true)
      }
    }
  }

  "Calling the updateWebsite() action in ConfirmWebsiteController" when {

    "there is a website in session" when {
        //TODO uncomment this test when confirmation has been added
//      "the website has been updated successfully" should {
//
//        "show the website changed success page" in {
//          mockIndividualAuthorised()
//          mockUpdateWebsite(vrn, testWebsite)(Future(Right(UpdatePPOBSuccess("success"))))
//          val result = TestConfirmWebsiteController.updateWebsite()(requestWithWebsite)
//
//          status(result) shouldBe Status.SEE_OTHER
//          redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/" +
//            "new-website-address-confirmation")
//        }
//      }

      "there was a conflict returned when trying to update the website" should {

        "redirect the user to the manage-vat page " in {
          mockIndividualAuthorised()
          mockUpdateWebsite(vrn, testWebsite)(
            Future(Left(ErrorModel(CONFLICT, "The back end has indicated there is an update already in progress"))))
          val result = TestConfirmWebsiteController.updateWebsite()(requestWithWebsite)

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
        }
      }

      "there was an unexpected error trying to update the website" should {

        "return an Internal Server Error" in {
          mockIndividualAuthorised()
          mockUpdateWebsite(vrn, testWebsite)(
            Future(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't verify website"))))
          val result = TestConfirmWebsiteController.updateWebsite()(requestWithWebsite)

          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
          messages(Jsoup.parse(bodyOf(result)).title) shouldBe internalServerErrorTitle
        }
      }
    }

    "there isn't a website in session" should {

      "take the user to the capture website page" in {
        mockIndividualAuthorised()
        val result = TestConfirmWebsiteController.updateWebsite()(request)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some("/vat-through-software/account/correspondence/new-website-address")
      }
    }

    "the user is not authorised" should {

      "return forbidden (403)" in {
        mockIndividualWithoutEnrolment()
        val result = TestConfirmWebsiteController.updateWebsite()(requestWithWebsite)

        status(result) shouldBe Status.FORBIDDEN
      }
    }

    "the changeWebsite feature is disabled" should {

      "present the server error page" in {
        mockConfig.features.changeWebsiteEnabled(false)
        mockIndividualAuthorised()

        val result = TestConfirmWebsiteController.updateWebsite()(request)

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        mockConfig.features.changeWebsiteEnabled(true)
      }
    }
  }
}
