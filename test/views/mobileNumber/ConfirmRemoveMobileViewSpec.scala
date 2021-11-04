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

package views.mobileNumber

import assets.BaseTestConstants.testValidationMobile
import controllers.mobileNumber.routes
import forms.YesNoForm
import models.YesNo
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import play.api.data.Form
import views.ViewBaseSpec
import views.html.mobileNumber.ConfirmRemoveMobileView

class ConfirmRemoveMobileViewSpec extends ViewBaseSpec with Matchers {

  val injectedView: ConfirmRemoveMobileView = inject[ConfirmRemoveMobileView]
  val form: Form[YesNo] = YesNoForm.yesNoForm("confirmRemoveMobile.error")

  "The ConfirmRemoveMobile page" when {

    "the user is a principal entity" should {

      lazy val view = injectedView(form, testValidationMobile)(user, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct title" in {
        document.title shouldBe
          "Are you sure you want to remove the mobile number? - Manage your VAT account - GOV.UK"
      }

      "have the correct heading" in {
        elementText("h1") shouldBe "Are you sure you want to remove the mobile number?"
      }

      "have a form" which {

        "has the correct action" in {
          element("form").attr("action") shouldBe routes.ConfirmRemoveMobileController.removeMobileNumber().url
        }

        "has a Yes option" in {
          elementText("#label-yes") shouldBe "Yes"
        }

        "has a No option" in {
          elementText("#label-no") shouldBe "No"
        }

        "has a continue button with the correct text" in {
          elementText(".govuk-button") shouldBe "Confirm and continue"
        }

        "has the correct error text" in {
          val errorForm = YesNoForm.yesNoForm("confirmRemoveMobile.error").bind(Map(YesNoForm.yesNo -> ""))
          lazy val view = injectedView(errorForm, testValidationMobile)(user, messages, mockConfig)
          lazy implicit val document: Document = Jsoup.parse(view.body)

          elementText("#yes_no-error") shouldBe "Error: Select yes if you want us to remove the mobile number"
        }
      }
    }

    "the user is an agent" should {

      lazy val view = injectedView(form, testValidationMobile)(agent, messages, mockConfig)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      "have the correct title" in {
        document.title shouldBe
          "Are you sure you want to remove the mobile number? - Your client’s VAT details - GOV.UK"
      }
    }
  }
}
