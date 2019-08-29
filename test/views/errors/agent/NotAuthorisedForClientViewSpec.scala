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

package views.errors.agent

import java.net.URLEncoder

import assets.BaseTestConstants.{arn, vrn}
import common.EnrolmentKeys
import models.User
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import views.ViewBaseSpec
import views.html.errors.agent.NotAuthorisedForClientView

class NotAuthorisedForClientViewSpec extends ViewBaseSpec {

  val injectedView: NotAuthorisedForClientView = injector.instanceOf[NotAuthorisedForClientView]

  "Rendering the unauthorised page" should {

    object Selectors {
      val serviceName = ".header__menu__proposition-name"
      val pageHeading = "#content h1"
      val instructions = "#content form > p"
      val instructionsLink = "#content form > p > a"
      val tryAgain = "#content article > p"
      val tryAgainLink = "#content article > p > a"
      val form = "#agentInviteForm"
      val hiddenService = s"$form input[name=service]"
      val hiddenIdentifierType = s"$form input[name=clientIdentifierType]"
      val hiddenIdentifier = s"$form input[name=clientIdentifier]"
      val button = "#content .button"
    }

    lazy val agent: User[AnyContentAsEmpty.type] = User[AnyContentAsEmpty.type](vrn, active = true, Some(arn))

    lazy val view = injectedView(vrn)(agent, messages, mockConfig)
    lazy implicit val document: Document = Jsoup.parse(view.body)

    "have the correct document title" in {
      document.title shouldBe "You are not authorised for this client - Your client’s VAT details - GOV.UK"
    }

    "have a the correct page heading" in {
      elementText(Selectors.pageHeading) shouldBe "You are not authorised for this client"
    }

    "have the correct instructions on the page" in {
      elementText(Selectors.instructions) shouldBe "To use this service, your client needs to authorise you as their agent."
    }

    "have the correct content for trying again" in {
      elementText(Selectors.tryAgain) shouldBe "If you think you have entered the wrong details you can try again."
    }

    "have a link to agent client sign up" in {
      element(Selectors.tryAgainLink).attr("href") shouldBe "mockManageVatOverviewUrl"
    }

    "have a form" which {

      s"has a POST action to ${mockConfig.agentInvitationsFastTrack}" in {
        val form = element(Selectors.form)
        form.attr("method") shouldBe "POST"
        form.attr("action") shouldBe s"${mockConfig.agentInvitationsFastTrack}?continue=" +
          s"${URLEncoder.encode(mockConfig.manageVatSubscriptionServicePath, "UTF-8")}"
      }

      "has a hidden field for the VAT service enrolment id" in {
        val input = element(Selectors.hiddenService)
        input.attr("value") shouldBe EnrolmentKeys.vatEnrolmentId
        input.attr("type") shouldBe "hidden"
      }

      "has a hidden field for the VAT service identifier id" in {
        val input = element(Selectors.hiddenIdentifierType)
        input.attr("value") shouldBe EnrolmentKeys.vatIdentifierId.toLowerCase
        input.attr("type") shouldBe "hidden"
      }

      "has a hidden field for the VRN" in {
        val input = element(Selectors.hiddenIdentifier)
        input.attr("value") shouldBe vrn
        input.attr("type") shouldBe "hidden"
      }

      "has a link which submits the form" in {
        val input = element(Selectors.instructionsLink)
        input.attr("onClick").contains("document.getElementById('agentInviteForm').submit()") shouldBe true
      }
    }

    "have a Sign out button" in {
      elementText(Selectors.button) shouldBe "Sign out"
    }

    "have a link to sign out" in {
      element(Selectors.button).attr("href") shouldBe
        controllers.routes.SignOutController.signOut(feedbackOnSignOut = false).url
    }
  }
}
