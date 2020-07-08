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

package pages

import common.SessionKeys
import helpers.IntegrationBaseSpec
import play.api.i18n.Messages
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{INTERNAL_SERVER_ERROR, UNAUTHORIZED}

trait BasePageISpec extends IntegrationBaseSpec {

  def formatSessionVrn: Option[String] => Map[String, String] =
    _.fold(Map.empty[String, String])(x => Map(SessionKeys.clientVrn -> x))

  def formatValidationEmail: Option[String] => Map[String, String] =
    _.fold(Map.empty[String, String])(x => Map(SessionKeys.validationEmailKey -> x))

  def formatEmail: Option[String] => Map[String, String] =
    _.fold(Map.empty[String, String])(x => Map(SessionKeys.prepopulationEmailKey -> x))

  def formatInflightChange: Option[String] => Map[String, String] =
    _.fold(Map.empty[String, String])(x => Map(SessionKeys.inFlightContactDetailsChangeKey -> x))

  def formatEmailPrefUpdate: Option[String] => Map[String, String] =
    _.fold(Map.empty[String, String])(value => Map(SessionKeys.contactPrefUpdate -> value))

  def formatEmailPrefConfirmation: Option[String] => Map[String, String] =
    _.fold(Map.empty[String, String])(value => Map(SessionKeys.contactPrefConfirmed -> value))

  def formatLetterToEmailPrefConfirmation: Option[String] => Map[String, String] =
    _.fold(Map.empty[String, String])(value => Map(SessionKeys.letterToEmailChangeSuccessful -> value))

  def httpPostAuthenticationTests(path: String, sessionVrn: Option[String] = None)(formData: Map[String, Seq[String]]): Unit =
    authenticationTests(path, post(path, formatSessionVrn(sessionVrn))(formData))

  def httpGetAuthenticationTests(path: String, sessionVrn: Option[String] = None): Unit =
    authenticationTests(path, get(path, formatSessionVrn(sessionVrn)))

  def generateDocumentTitle(message: String, isAgent: Option[Boolean] = Some(false)): String =
    messages("base.pageTitle", messages(message),
      isAgent match {
        case Some(agent) =>
          if (agent) messages("common.agentService") else messages("common.clientService")
        case _ => messages("common.vat")
      }
    )


  private def authenticationTests(path: String, method: => WSResponse): Unit = {

    "the user is timed out (not authenticated)" should {

      "Render the session timeout view" in {

        given.user.isNotAuthenticated

        When(s"I call the path '$path'")
        val res = method

        res should have(
          httpStatus(UNAUTHORIZED),
          pageTitle(Messages("sessionTimeout.title"))
        )
      }
    }

    "the user is logged in without an Affinity Group" should {

      "Render the Internal Server Error view" in {

        given.user.noAffinityGroup

        When(s"I call the path '$path'")
        val res = method

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR),
          pageTitle(Messages("standardError.title"))
        )
      }
    }
  }
}
