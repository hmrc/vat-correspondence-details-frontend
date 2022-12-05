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

package controllers.email

import common.SessionKeys._
import controllers.ControllerBaseSpec
import models.errors.ErrorModel
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.templates.ChangeSuccessView
import assets.BaseTestConstants.vrn
import assets.CustomerInfoConstants.fullCustomerInfoModel
import org.jsoup.Jsoup

class EmailChangeSuccessControllerSpec extends ControllerBaseSpec {

  val view: ChangeSuccessView = inject[ChangeSuccessView]

  object TestController extends EmailChangeSuccessController(
    mockVatSubscriptionService,
    view
  )

  val successfulChangeRequest: FakeRequest[AnyContentAsEmpty.type] =
    getRequest.withSession(emailChangeSuccessful -> "true")

  "Calling the show action" when {

    "a user is enrolled with a valid enrolment" when {

      "the user has the email change success key in the session" when {

        "a valid response is retrieved from the customer info service" should {

          "a digital preference is retrieved" should {

            lazy val result = {
              mockIndividualAuthorised
              mockGetCustomerInfo(vrn)(Right(fullCustomerInfoModel))
              TestController.show(successfulChangeRequest)
            }
            lazy val document = Jsoup.parse(contentAsString(result))

            "return 200" in {
              status(result) shouldBe Status.OK
            }

            "display the digital preference message" in {
              document.select("#preference-message").text() shouldBe
                "We’ll send you an email within 2 working days with an update or you can check your HMRC secure messages."
            }
          }

          "a paper preference is retrieved" should {

            lazy val result = {
              mockIndividualAuthorised
              mockGetCustomerInfo(vrn)(Right(fullCustomerInfoModel.copy(commsPreference = Some("PAPER"))))
              TestController.show(successfulChangeRequest)
            }
            lazy val document = Jsoup.parse(contentAsString(result))

            "return 200" in {
              status(result) shouldBe Status.OK
            }

            "display the preference message" in {
              document.select("#preference-message").text() shouldBe
                "We’ll send a letter to your principal place of business with an update within 15 working days."
            }
          }
        }

        "an invalid response is retrieved from the customer info service" should {

          lazy val result = {
            mockIndividualAuthorised
            mockGetCustomerInfo(vrn)(Left(ErrorModel(Status.BAD_GATEWAY, "Error")))
            TestController.show(successfulChangeRequest)
          }
          lazy val document = Jsoup.parse(contentAsString(result))

          "return 200" in {
            status(result) shouldBe Status.OK
          }

          "display the generic preference message" in {
            document.select("#content p:nth-child(3)").text() shouldBe
              "We will send you an update within 15 working days."
          }
        }
      }

      "the user does not have the email change success key in the session" should {

        lazy val result = {
          mockIndividualAuthorised
          TestController.show(getRequest)
        }

        "return 303" in {
          status(result) shouldBe Status.SEE_OTHER
        }

        "redirect to the capture email page" in {
          redirectLocation(result) shouldBe Some(controllers.email.routes.CaptureEmailController.show.url)
        }
      }
    }

    "a user is does not have a valid enrolment" should {

      lazy val result = {
        mockIndividualWithoutEnrolment
        TestController.show(getRequest)
      }

      "return 403" in {
        status(result) shouldBe Status.FORBIDDEN
      }

      "return HTML" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }
    }

    insolvencyCheck(TestController.show())
  }
}
