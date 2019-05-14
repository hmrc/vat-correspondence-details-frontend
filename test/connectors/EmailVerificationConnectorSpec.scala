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

package connectors

import controllers.ControllerBaseSpec
import uk.gov.hmrc.play.bootstrap.http.HttpClient

class EmailVerificationConnectorSpec extends ControllerBaseSpec {

  lazy val connector = new EmailVerificationConnector(mock[HttpClient], mockConfig)

  "EmailVerificationConnector" should {
    "generate the correct verification request URL" in {
      connector.createEmailVerificationRequestUrl shouldBe "mockEmailBaseUrl/email-verification/verification-requests"
    }
  }
}
