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

package mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Writes
import uk.gov.hmrc.http.HttpReads
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

trait MockHttp extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  val mockHttp: HttpClient = mock[HttpClient]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHttp)
  }

  def setupMockHttpGet[T](url: String)(response: T): OngoingStubbing[Future[T]] =
    when(mockHttp.GET[T](ArgumentMatchers.eq(url))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(response))

  def setupMockHttpPost[I,O](url: String)(response: O): OngoingStubbing[Future[O]] =
    when(mockHttp.POST[I,O]
      (ArgumentMatchers.eq(url), ArgumentMatchers.any[I](), ArgumentMatchers.any())
      (ArgumentMatchers.any[Writes[I]](),ArgumentMatchers.any[HttpReads[O]](), ArgumentMatchers.any(), ArgumentMatchers.any())
    ).thenReturn(Future.successful(response))

  def setupMockHttpPut[I,O](url: String)(response: O): OngoingStubbing[Future[O]] =
    when(mockHttp.PUT[I,O]
      (ArgumentMatchers.eq(url), ArgumentMatchers.any[I]())
      (ArgumentMatchers.any[Writes[I]](),ArgumentMatchers.any[HttpReads[O]](), ArgumentMatchers.any(), ArgumentMatchers.any())
    ).thenReturn(Future.successful(response))
}
