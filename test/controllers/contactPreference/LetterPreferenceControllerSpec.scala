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

package controllers.contactPreference

import assets.BaseTestConstants._
import assets.CustomerInfoConstants._
import assets.{CustomerInfoConstants, LetterPreferenceMessages}
import common.SessionKeys
import controllers.ControllerBaseSpec
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK, SEE_OTHER}
import play.api.test.Helpers._
import forms.YesNoForm.{yes, yesNo, no => _no}
import mocks.MockVatSubscriptionService
import models.contactPreferences.ContactPreference
import models.errors.ErrorModel
import org.jsoup.Jsoup
import play.api.http.Status
import views.html.contactPreference.LetterPreferenceView
import models.contactPreferences.ContactPreference._
import models.customerInformation.UpdatePPOBSuccess
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

import scala.concurrent.Future

class LetterPreferenceControllerSpec extends ControllerBaseSpec with MockVatSubscriptionService {

  lazy val view: LetterPreferenceView = inject[LetterPreferenceView]
  lazy val controller = new LetterPreferenceController(view, mockVatSubscriptionService, mockErrorHandler)
  lazy val requestWithSession: FakeRequest[AnyContentAsEmpty.type] = request.withSession((SessionKeys.currentContactPrefKey -> digital))

  "Calling .show()" when {

    "the user is authorised" when {

      "the user currently has a digital preference" when {

        "the letterToConfirmedEmailEnabled feature switch is off" should {

          lazy val result = {
            mockConfig.features.letterToConfirmedEmailEnabled(false)
            controller.show(requestWithSession)
          }

          "return a NOT_FOUND result" in {
            status(result) shouldBe NOT_FOUND
          }
        }

        "the letterToConfirmedEmailEnabled feature switch is on" when {

          "call to customer info is successful" when {

            "user has a postcode" should {

              lazy val result = {
                mockConfig.features.letterToConfirmedEmailEnabled(true)
                mockGetCustomerInfo(vrn)(Future.successful(Right(fullCustomerInfoModel)))
                controller.show(requestWithSession)
              }

              lazy val page = Jsoup.parse(bodyOf(result))

              "return an OK result" in {
                status(result) shouldBe OK
              }

              "return the LetterPreference view" in {
                page.title should include(LetterPreferenceMessages.heading)
              }

              "show first line of address and postcode" in {
                page.select("label[for=yes_no-yes]").text() should include("firstLine, codeOfMyPost")
              }
            }

            "user does not have a postcode" should {

              lazy val result = {
                mockConfig.features.letterToConfirmedEmailEnabled(true)
                mockGetCustomerInfo(vrn)(Future.successful(Right(minCustomerInfoModel)))
                controller.show(requestWithSession)
              }

              lazy val page = Jsoup.parse(bodyOf(result))

              "return an OK result" in {
                status(result) shouldBe OK
              }

              "show first line of address" in {
                page.select("label[for=yes_no-yes]").text() should include("firstLine")
              }
            }
          }

          "call to customer info is unsuccessful" should {

            lazy val result = {
              mockConfig.features.letterToConfirmedEmailEnabled(true)
              mockGetCustomerInfo(vrn)(Future.successful(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, ""))))
              controller.show(requestWithSession)
            }

            "return an INTERNAL_SERVER_ERROR result" in {
              status(result) shouldBe INTERNAL_SERVER_ERROR
            }
          }
        }
      }

      "the user currently has a paper preference" should {

        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          controller.show(request.withSession(SessionKeys.currentContactPrefKey -> paper))
        }

        "return an SEE_OTHER result" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to BTA" in {
          redirectLocation(result) shouldBe Some(mockConfig.btaAccountDetailsUrl)
        }
      }
    }

    "user is unauthorised" should {

      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(true)
        mockIndividualWithoutEnrolment()
        controller.show(request)
      }

      "return a FORBIDDEN result" in {
        status(result) shouldBe FORBIDDEN
      }
    }
  }

  "calling .submit()" when {

    "the user is authorised" when {

      "the user currently has a digital preference" when {

        "the letterToConfirmedEmailEnabled feature switch is off" when {

          lazy val result = {
            mockConfig.features.letterToConfirmedEmailEnabled(false)
            controller.submit(requestWithSession)
          }

          "return a NOT_FOUND result" in {
            status(result) shouldBe NOT_FOUND
          }
        }

        "the letterToConfirmedEmailEnabled feature switch is on" when {

          "'Yes' is submitted" should {

            val yesRequest = requestWithSession.withFormUrlEncodedBody(yesNo -> yes)

            "the contact preference has been updated successfully" should {

              lazy val result = {
                mockConfig.features.letterToConfirmedEmailEnabled(true)
                mockIndividualAuthorised()
                mockUpdateContactPreference(
                  vrn, ContactPreference.paper)(Future(Right(UpdatePPOBSuccess("success")))
                )
                controller.submit(yesRequest)
              }

              "return 303 (SEE OTHER)" in {
                status(result) shouldBe Status.SEE_OTHER
              }

              s"Redirect to the '${controllers.contactPreference.routes.ContactPreferenceConfirmationController.show("letter").url}'" in {
                redirectLocation(result) shouldBe Some(controllers.contactPreference.routes.ContactPreferenceConfirmationController.show("letter").url)
              }
            }

            "there was a conflict returned when trying to update the contact preference" should {

              lazy val result = {
                mockConfig.features.letterToConfirmedEmailEnabled(true)
                mockIndividualAuthorised()
                mockUpdateContactPreference(
                  vrn, ContactPreference.paper)(Future(Left(ErrorModel(CONFLICT, "The back end has indicated there is an update already in progress")))
                )
                controller.submit(yesRequest)
              }

              "return 303" in {
                status(result) shouldBe Status.SEE_OTHER
              }

              "redirect the user to the manage-vat overview page" in {
                redirectLocation(result) shouldBe Some(mockConfig.manageVatSubscriptionServicePath)
              }
            }

            "there was an unexpected error trying to update the contact preference" should {

              lazy val result = {
                mockConfig.features.letterToConfirmedEmailEnabled(true)
                mockIndividualAuthorised()
                mockUpdateContactPreference(vrn, ContactPreference.paper)(
                  Future(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't update contact preference"))))
                controller.submit(yesRequest)
              }

              "return 500" in {
                status(result) shouldBe Status.INTERNAL_SERVER_ERROR
              }

              "show the internal server error page" in {
                messages(Jsoup.parse(bodyOf(result)).title) shouldBe internalServerErrorTitle
              }
            }
          }

          "'No' is submitted'" should {

            lazy val result = {
              mockConfig.features.letterToConfirmedEmailEnabled(true)
              controller.submit(requestWithSession.withFormUrlEncodedBody(yesNo -> _no))
            }

            "return a SEE_OTHER result" in {
              status(result) shouldBe SEE_OTHER
            }

            "redirect to BTA" in {
              redirectLocation(result) shouldBe Some(mockConfig.btaAccountDetailsUrl)
            }
          }

          "Nothing is submitted (form has errors)" when {

            "call to customer info is successful" should {
              lazy val result = {
                mockConfig.features.letterToConfirmedEmailEnabled(true)
                mockGetCustomerInfo(vrn)(Future.successful(Right(fullCustomerInfoModel)))
                controller.submit(requestWithSession.withFormUrlEncodedBody())
              }

              "return a BAD_REQUEST result" in {
                status(result) shouldBe BAD_REQUEST
              }

              "return the LetterPreference view with errors" in {
                Jsoup.parse(bodyOf(result)).title should include("Error: " + LetterPreferenceMessages.heading)
              }
            }

            "call to customer info is unsuccessful" should {

              lazy val result = {
                mockConfig.features.letterToConfirmedEmailEnabled(true)
                mockGetCustomerInfo(vrn)(Future.successful(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, ""))))
                controller.submit(requestWithSession.withFormUrlEncodedBody())
              }

              "return an INTERNAL_SERVER_ERROR result" in {
                status(result) shouldBe INTERNAL_SERVER_ERROR
              }
            }
          }
        }
      }

      "the user currently has a paper preference" should {

        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          controller.submit(request.withSession(SessionKeys.currentContactPrefKey -> paper))
        }

        "return an SEE_OTHER result" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to BTA" in {
          redirectLocation(result) shouldBe Some(mockConfig.btaAccountDetailsUrl)
        }
      }
    }

    "user is unauthorised" should {

      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(true)
        mockIndividualWithoutEnrolment()
        controller.submit(requestWithSession)
      }

      "return a FORBIDDEN result" in {
        status(result) shouldBe FORBIDDEN
      }
    }
  }

  "Calling .displayAddress()" when {

    "PPOB has a postcode" should {

      lazy val result = controller.displayAddress(CustomerInfoConstants.fullPPOBModel)

      "return the right string" in {
        result shouldBe "firstLine, codeOfMyPost"
      }
    }

    "PPOB does not have a postcode" should {

      lazy val result = controller.displayAddress(CustomerInfoConstants.minPPOBModel)

      "return the right string" in {
        result shouldBe "firstLine"
      }
    }
  }
}
