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

package controllers.email

import audit.models.ContactPreferenceAuditModel
import common.SessionKeys._
import controllers.ControllerBaseSpec
import mocks.{MockAuditingService, MockContactPreferenceService}
import models.contactPreferences.ContactPreference
import models.errors.ErrorModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.verify
import play.api.http.Status
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.email.EmailChangeSuccessView

import scala.concurrent.{ExecutionContext, Future}

class EmailChangeSuccessControllerSpec extends ControllerBaseSpec with MockContactPreferenceService with MockAuditingService {

  val view: EmailChangeSuccessView = injector.instanceOf[EmailChangeSuccessView]

  object TestController extends EmailChangeSuccessController(
    mockAuditingService,
    mockContactPreferenceService,
    view
  )

  "Calling the show action" when {

    "a user is enrolled with a valid enrolment" when {

      "a valid response is retrieved from the contact preference service" should {

        "a digital preference is retrieved" should {
          lazy val result = TestController.show(request.withSession(
            prepopulationEmailKey -> "myemail@gmail.com",
            validationEmailKey -> "anotheremail@gmail.com"
          ))

          lazy val document = Jsoup.parse(bodyOf(result))

          "return 200" in {
            mockConfig.features.contactPreferencesEnabled(true)
            mockIndividualAuthorised()
            getMockContactPreference("999999999")(Future(Right(ContactPreference("DIGITAL"))))
            status(result) shouldBe Status.OK

            verify(mockAuditingService)
              .extendedAudit(
                ArgumentMatchers.any[ContactPreferenceAuditModel],
                ArgumentMatchers.any[String]

              )(
                ArgumentMatchers.any[HeaderCarrier],
                ArgumentMatchers.any[ExecutionContext]
              )
          }

          "render the email change success page" in {
            mockIndividualAuthorised()
            messages(document.select("#content article p:nth-of-type(1)").text()) shouldBe
              "We will send you an email within 2 working days with an update, followed by a letter to your " +
                "principal place of business. You can also go to your HMRC secure messages to find out if your " +
                "request has been accepted."
          }
        }

        "a paper preference is retrieved" should {

          lazy val result = TestController.show(request.withSession(
            prepopulationEmailKey -> "myemail@gmail.com",
            validationEmailKey -> "anotheremail@gmail.com"
          ))

          lazy val document = Jsoup.parse(bodyOf(result))

          "return 200" in {
            mockConfig.features.contactPreferencesEnabled(true)
            mockIndividualAuthorised()
            getMockContactPreference("999999999")(Future(Right(ContactPreference("PAPER"))))
            status(result) shouldBe Status.OK

            verify(mockAuditingService)
              .extendedAudit(
                ArgumentMatchers.any[ContactPreferenceAuditModel],
                ArgumentMatchers.any[String]

              )(
                ArgumentMatchers.any[HeaderCarrier],
                ArgumentMatchers.any[ExecutionContext]
              )
          }

          "render the email change success page" in {
            mockIndividualAuthorised()
            messages(document.select("#content article p:nth-of-type(1)").text()) shouldBe
              "We will send a letter to your principal place of business with an update within 15 working days."
          }
        }
      }

      "an invalid response is retrieved from the contact preference service" should {

        lazy val result = TestController.show(request)

        lazy val document = Jsoup.parse(bodyOf(result))

        "return 200" in {
          mockIndividualAuthorised()
          mockConfig.features.contactPreferencesEnabled(true)
          getMockContactPreference("999999999")(Future(Left(ErrorModel(Status.BAD_GATEWAY, "Error"))))
          status(result) shouldBe Status.OK
        }

        "return HTML" in {
          mockIndividualAuthorised()
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "remove the email session key from the session" in {
          session(result).get(prepopulationEmailKey) shouldBe None
        }

        "remove the validation email session key from the session" in {
          session(result).get(validationEmailKey) shouldBe None
        }

        "render the email change success page" in {
          mockIndividualAuthorised()
          messages(document.select("#content article p:nth-of-type(1)").text()) shouldBe
            "We will send you an update within 15 working days."
        }
      }

      "the contact preference feature switch is disabled" should {

        lazy val result = TestController.show(request.withSession(
          prepopulationEmailKey -> "myemail@gmail.com",
          validationEmailKey -> "anotheremail@gmail.com"
        ))
        lazy val document = Jsoup.parse(bodyOf(result))

        "return 200" in {
          mockIndividualAuthorised()
          mockConfig.features.contactPreferencesEnabled(false)
          status(result) shouldBe Status.OK
        }
        "return HTML" in {
          mockIndividualAuthorised()
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "remove the email session key from the session" in {
          session(result).get(prepopulationEmailKey) shouldBe None
        }

        "remove the validation email session key from the session" in {
          session(result).get(validationEmailKey) shouldBe None
        }

        "render the email change success page" in {
          mockIndividualAuthorised()
          messages(document.select("#content article p:nth-of-type(1)").text()) shouldBe
            "We will send an email within 2 working days telling you whether or not the request has been accepted. " +
            "You can also go to your messages in your business tax account."
        }
      }
    }


    "a user is does not have a valid enrolment" should {

      lazy val result = TestController.show(request)

      "return 403" in {
        mockIndividualWithoutEnrolment()
        status(result) shouldBe Status.FORBIDDEN
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }
  }
}
