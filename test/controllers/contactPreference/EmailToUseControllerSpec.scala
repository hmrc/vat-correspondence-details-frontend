/*
 * Copyright 2021 HM Revenue & Customs
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
import assets.CustomerInfoConstants.{customerInfoEmailUnverified, fullCustomerInfoModel}
import audit.models.DigitalContactPreferenceAuditModel
import common.SessionKeys
import connectors.httpParsers.ResponseHttpParser.HttpGetResult
import controllers.ControllerBaseSpec
import forms.YesNoForm.yesNo
import models.contactPreferences.ContactPreference
import models.customerInformation.{CustomerInformation, UpdatePPOBSuccess}
import models.errors.ErrorModel
import org.jsoup.Jsoup
import org.mockito.Mockito.reset
import play.api.http.Status
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.contactPreference.EmailToUseView

import scala.concurrent.Future

class EmailToUseControllerSpec extends ControllerBaseSpec {

  def mockVatSubscriptionCall(result: HttpGetResult[CustomerInformation] = Right(fullCustomerInfoModel)): Unit =
    mockGetCustomerInfo("999999999")(Future.successful(result))

  val testValidationEmail: String = "validation@example.com"

  lazy val existingEmailSessionRequest: FakeRequest[AnyContentAsEmpty.type] =
    requestWithPaperPref.withSession(
      SessionKeys.validationEmailKey -> testValidationEmail,
      SessionKeys.contactPrefUpdate -> "true"
    )

  lazy val noEmailSessionRequest: FakeRequest[AnyContentAsEmpty.type] =
    requestWithPaperPref.withSession(SessionKeys.contactPrefUpdate -> "true")

  lazy val noPrefUpdateValueSessionRequest: FakeRequest[AnyContentAsEmpty.type] =
    requestWithPaperPref.withSession(SessionKeys.validationEmailKey -> testValidationEmail)

  val view: EmailToUseView = injector.instanceOf[EmailToUseView]

  def target(): EmailToUseController = new EmailToUseController(mockVatSubscriptionService, view, mockAuditingService)

  "Calling the show action in EmailToUseController" when {

    "the letterToConfirmedEmail switch is enabled" when {

      s"there is an email and the ${SessionKeys.contactPrefUpdate} value is in session" should {

        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          target().show()(existingEmailSessionRequest)
        }

        "return 200 (OK)" in {
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "add the email address to session" in {
          session(result).get(SessionKeys.validationEmailKey) shouldBe Some(testValidationEmail)
          session(result).get(SessionKeys.prepopulationEmailKey) shouldBe Some(testValidationEmail)
        }
      }

      "there isn't an email in session" should {

        lazy val result = {
          mockVatSubscriptionCall()
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          target().show()(noEmailSessionRequest)
        }

        "return 200 (OK)" in {
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "add the email address to session" in {
          session(result).get(SessionKeys.validationEmailKey) shouldBe Some("pepsimac@gmail.com")
          session(result).get(SessionKeys.prepopulationEmailKey) shouldBe Some("pepsimac@gmail.com")
        }
      }

      s"the ${SessionKeys.contactPrefUpdate} value is not in session" should {

        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          target().show()(noPrefUpdateValueSessionRequest)
        }

        s"return a $SEE_OTHER" in {
          status(result) shouldBe SEE_OTHER
        }

        "redirect to the preference select page" in {
          redirectLocation(result) shouldBe Some(controllers.contactPreference.routes.EmailPreferenceController.show().url)
        }
      }
    }

    "the letterToConfirmedEmail switch is disabled" should {

      "return a 404" in {

        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(false)
          target().show()(existingEmailSessionRequest)
        }
        status(result) shouldBe Status.NOT_FOUND
      }
    }

    insolvencyCheck(target().show())
  }

  "Calling the submit action in EmailToUseController" when {

    "the letterToConfirmedEmail switch is enabled" when {

      "the user submits after selecting an 'Yes' option" when {

        lazy val yesRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
          requestWithPaperPref
            .withFormUrlEncodedBody((yesNo, "yes"))
            .withSession(
              SessionKeys.validationEmailKey -> testValidationEmail,
              SessionKeys.contactPrefUpdate -> "true"
            )

        "the user has a verified email address in ETMP" when {

          "the contact preference has been updated successfully" should {

            lazy val result = {
              mockConfig.features.letterToConfirmedEmailEnabled(true)
              mockVatSubscriptionCall()
              mockUpdateContactPreference(vrn, ContactPreference.digital)(Future(Right(UpdatePPOBSuccess("success"))))
              target().submit(yesRequest)
            }

            "return 303 (SEE OTHER)" in {
              status(result) shouldBe Status.SEE_OTHER
            }

            "audit the change landline number event" in {
              verifyExtendedAudit(
                DigitalContactPreferenceAuditModel(
                  testValidationEmail,
                  vrn,
                  ContactPreference.paper,
                  ContactPreference.digital
                )
              )
              reset(mockAuditingService)
            }

            "redirect to the confirmation controller" in {
              redirectLocation(result) shouldBe
                Some(controllers.contactPreference.routes.ContactPreferenceConfirmationController.show("email").url)
            }
          }

          "there was a conflict returned when trying to update the contact preference" should {

            lazy val result = {
              mockConfig.features.letterToConfirmedEmailEnabled(true)
              mockVatSubscriptionCall()
              mockUpdateContactPreference(vrn, ContactPreference.digital)(
                Future(Left(ErrorModel(CONFLICT, "The back end has indicated there is an update already in progress"))))
              target().submit(yesRequest)
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
              mockVatSubscriptionCall()
              mockUpdateContactPreference(vrn, ContactPreference.digital)(
                Future(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Couldn't update contact preference"))))
              target().submit(yesRequest)
            }

            "return 500" in {
              status(result) shouldBe Status.INTERNAL_SERVER_ERROR
            }

            "show the internal server error page" in {
              messages(Jsoup.parse(bodyOf(result)).title) shouldBe internalServerErrorTitle
            }
          }
        }

        "the user does not have a verified email address in ETMP" should {

          lazy val result = {
            mockConfig.features.letterToConfirmedEmailEnabled(true)
            mockVatSubscriptionCall(Right(customerInfoEmailUnverified))
            target().submit(yesRequest)
          }

          "return 303" in {
            status(result) shouldBe Status.SEE_OTHER
          }

          "redirect the user to the verification route to update email and contact preference" in {
            redirectLocation(result) shouldBe Some(controllers.email.routes.VerifyEmailController.updateContactPrefEmail().url)
          }
        }

        "the customer info call fails" should {

          lazy val result = {
            mockConfig.features.letterToConfirmedEmailEnabled(true)
            mockVatSubscriptionCall(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, "Error")))
            target().submit(yesRequest)
          }

          "return 500" in {
            status(result) shouldBe Status.INTERNAL_SERVER_ERROR
          }
        }
      }

      "the user submits after selecting an 'No' option" should {

        lazy val yesRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
          requestWithPaperPref
            .withFormUrlEncodedBody((yesNo, "no"))
            .withSession(
              SessionKeys.validationEmailKey -> testValidationEmail,
              SessionKeys.contactPrefUpdate -> "true"
            )
        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          target().submit()(yesRequest)
        }

        "return 303 (SEE OTHER)" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        s"Redirect to the '${controllers.email.routes.CaptureEmailController.showPrefJourney().url}'" in {
          redirectLocation(result) shouldBe Some(controllers.email.routes.CaptureEmailController.showPrefJourney().url)
        }
      }

      "the user submits without selecting an option" should {

        lazy val yesRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
          requestWithPaperPref
            .withFormUrlEncodedBody((yesNo, ""))
            .withSession(
              SessionKeys.validationEmailKey -> testValidationEmail,
              SessionKeys.contactPrefUpdate -> "true"
            )
        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          target().submit()(yesRequest)
        }

        "return a 400" in {
          status(result) shouldBe Status.BAD_REQUEST
        }
      }

      "the user does not have an email in session" should {

        lazy val yesRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
          requestWithPaperPref
            .withFormUrlEncodedBody((yesNo, "no"))
            .withSession(SessionKeys.contactPrefUpdate -> "true")
        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          target().submit()(yesRequest)
        }

        "return an ISE" in {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      s"the ${SessionKeys.contactPrefUpdate} key is not in session" should {

        lazy val yesRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
          requestWithPaperPref
            .withFormUrlEncodedBody((yesNo, "yes"))
            .withSession(SessionKeys.validationEmailKey -> testValidationEmail)

        lazy val result = {
          mockConfig.features.letterToConfirmedEmailEnabled(true)
          target().submit()(yesRequest)
        }

        "return 303 (SEE OTHER)" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        s"Redirect to the '${controllers.contactPreference.routes.EmailPreferenceController.show().url}'" in {
          redirectLocation(result) shouldBe Some(controllers.contactPreference.routes.EmailPreferenceController.show().url)
        }
      }
    }

    "the letterToConfirmedEmail switch is disabled" when {

      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(false)
        target().submit()(requestWithPaperPref)
      }

      "return a 404" in {
        status(result) shouldBe Status.NOT_FOUND
      }
    }

    insolvencyCheck(target().submit())
  }
}
