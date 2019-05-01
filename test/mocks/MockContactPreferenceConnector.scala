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

package mocks

import connectors.ContactPreferenceConnector
import connectors.httpParsers.ResponseHttpParser.HttpGetResult
import models.contactPreferences.ContactPreference
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockContactPreferenceConnector extends MockitoSugar with BeforeAndAfterEach {
  this: Suite =>

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockContactPreferenceConnector)
  }

  val mockContactPreferenceConnector: ContactPreferenceConnector = mock[ContactPreferenceConnector]

  def mockGetContactPreference(vrn: String)(response: Future[HttpGetResult[ContactPreference]]): Unit =
    when(mockContactPreferenceConnector.getContactPreference(
      ArgumentMatchers.eq(vrn)
    )(ArgumentMatchers.any[HeaderCarrier], ArgumentMatchers.any[ExecutionContext])) thenReturn response

}
