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

package audit

import audit.models.AuditModel
import controllers.ControllerBaseSpec
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.{verify, when}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import org.mockito.Mockito.{mock => mMock}

import scala.concurrent.{ExecutionContext, Future}

class AuditServiceSpec extends ControllerBaseSpec {

  lazy val mockAuditConnector: AuditConnector = mMock[AuditConnector](classOf[AuditConnector])
  val auditingService = new AuditingService(mockConfig, mockAuditConnector)

  "The Auditing Service" should {

    "extract extended data from an audit model and pass it to the Audit Connector" in {

      case class TestAuditModel(testString: String) extends AuditModel {
        val transactionName = "testAudit"
        val detail: JsValue = Json.parse(s"""{"test":"$testString"}""")
        val auditType = "testType"
      }

      val testModel = TestAuditModel("woohoo")

      val expectedData: ExtendedDataEvent = auditingService.toExtendedDataEvent("", testModel, "")

      when(mockAuditConnector.sendExtendedEvent(refEq(expectedData, "eventId", "generatedAt"))
                                               (any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(AuditResult.Success))

      auditingService.extendedAudit(testModel, "")

      verify(mockAuditConnector).sendExtendedEvent(
        refEq(expectedData, "eventId", "generatedAt"))(any[HeaderCarrier], any[ExecutionContext]
      )
    }
  }
}
