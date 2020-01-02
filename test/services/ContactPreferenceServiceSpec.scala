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

package services

import assets.BaseTestConstants.vrn
import connectors.httpParsers.ResponseHttpParser.HttpGetResult
import mocks.MockContactPreferenceConnector
import models.contactPreferences.ContactPreference
import models.errors.ErrorModel
import org.scalatest.EitherValues
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestUtil

import scala.concurrent.Future

class ContactPreferenceServiceSpec extends UnitSpec with MockContactPreferenceConnector with TestUtil with EitherValues {

  object TestContactPreferenceService extends ContactPreferenceService(
    mockContactPreferenceConnector
  )

  "Calling .getContactPreference" when {

    def getContactPreferenceResult: Future[HttpGetResult[ContactPreference]] = TestContactPreferenceService.getContactPreference(vrn)

    "the service returns a success" should {

      "return a ContactPreference model" in {
        mockGetContactPreference(vrn)(Right(ContactPreference("DIGITAL")))
        await(getContactPreferenceResult) shouldBe Right(ContactPreference("DIGITAL"))
      }
    }

    "the service returns an error" should {

      val errorModel: ErrorModel = ErrorModel(1, "Error")

      "return an ErrorModel" in {
        mockGetContactPreference(vrn)(Left(errorModel))
        await(getContactPreferenceResult) shouldBe Left(errorModel)
      }
    }
  }
}
