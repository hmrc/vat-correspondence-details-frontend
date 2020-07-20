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

import assets.BaseTestConstants.vrn
import assets.CustomerInfoConstants.fullCustomerInfoModel
import controllers.ControllerBaseSpec
import play.api.http.Status.{NOT_FOUND, OK, SEE_OTHER}
import views.html.contactPreference.PreferenceConfirmationView
import common.SessionKeys._
import models.errors.ErrorModel
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.test.Helpers._

import scala.concurrent.Future

class ContactPreferenceConfirmationControllerSpec extends ControllerBaseSpec {

  lazy val controller = new ContactPreferenceConfirmationController(inject[PreferenceConfirmationView], mockVatSubscriptionService)

  ".show" when {

    "the feature switch is false" should {

      lazy val result = {
        mockConfig.features.letterToConfirmedEmailEnabled(false)
        controller.show("email")(requestWithPaperPref)
      }

      "return a NOT_FOUND result" in {
        status(result) shouldBe NOT_FOUND
      }
    }

    "the feature switch is true" when {

      "changeType is email" when {

        s"$letterToEmailChangeSuccessful is in session" when {

          s"$validationEmailKey is in session" should {

            lazy val result = {
              mockConfig.features.letterToConfirmedEmailEnabled(true)
              controller.show("email")(requestWithPaperPref.withSession(
                validationEmailKey -> "asd@asd.com",
                letterToEmailChangeSuccessful -> "true"
              ))
            }

            "return OK" in {
              status(result) shouldBe OK
            }

            "render view with the email populated" in {
              Jsoup.parse(bodyOf(result)).select("#validationValue").text() shouldBe "asd@asd.com"
            }
          }

          s"$validationEmailKey is not in session" should {

            lazy val result = {
              mockConfig.features.letterToConfirmedEmailEnabled(true)
              controller.show("email")(requestWithPaperPref.withSession(
                letterToEmailChangeSuccessful -> "true"
              ))
            }

            "return 303" in {
              status(result) shouldBe SEE_OTHER
            }

            s"redirect to ${controllers.contactPreference.routes.EmailToUseController.show().url}" in {
              redirectLocation(result) shouldBe Some(controllers.contactPreference.routes.EmailToUseController.show().url)
            }
          }
        }

        s"$letterToEmailChangeSuccessful is not in session" should {

          lazy val result = {
            mockConfig.features.letterToConfirmedEmailEnabled(true)
            controller.show("email")(requestWithPaperPref)
          }

          "return 303" in {
            status(result) shouldBe SEE_OTHER
          }

          s"redirect to ${controllers.contactPreference.routes.EmailToUseController.show().url}" in {
            redirectLocation(result) shouldBe Some(controllers.contactPreference.routes.EmailToUseController.show().url)
          }
        }
      }

      "changeType is letter" when {

        s"$emailToLetterChangeSuccessful is in session" when {

          "retrieval of current address is successful" should {

            lazy val result = {
              mockConfig.features.letterToConfirmedEmailEnabled(true)
              mockGetCustomerInfo(vrn)(Future.successful(Right(fullCustomerInfoModel)))
              controller.show("letter")(requestWithDigitalPref.withSession(
                emailToLetterChangeSuccessful -> "true"
              ))
            }

            "return OK" in {
              status(result) shouldBe OK
            }

            "render view with the full business address populated" in {
              Jsoup.parse(bodyOf(result)).select("#validationValue").text() shouldBe
                "firstLine secondLine thirdLine fourthLine fifthLine codeOfMyPost"
            }
          }

          "retrieval of current address is unsuccessful" should {

            lazy val result = {
              mockGetCustomerInfo(vrn)(Future.successful(Left(ErrorModel(Status.INTERNAL_SERVER_ERROR, ""))))
              controller.show("letter")(requestWithDigitalPref.withSession(
                emailToLetterChangeSuccessful -> "true"
              ))
            }

            "return 500" in {
              status(result) shouldBe INTERNAL_SERVER_ERROR
            }
          }
        }

        s"$emailToLetterChangeSuccessful is not in session" should {

          lazy val result = {
            mockConfig.features.letterToConfirmedEmailEnabled(true)
            controller.show("letter")(requestWithDigitalPref)
          }

          "return 303" in {
            status(result) shouldBe SEE_OTHER
          }

          s"redirect to ${controllers.contactPreference.routes.LetterPreferenceController.show().url}" in {
            redirectLocation(result) shouldBe Some(controllers.contactPreference.routes.LetterPreferenceController.show().url)
          }
        }
      }
    }
  }
}