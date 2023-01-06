/*
 * Copyright 2023 HM Revenue & Customs
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
import config.AppConfig
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import utils.LoggerUtil
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditingService @Inject()(appConfig: AppConfig, auditConnector: AuditConnector) extends LoggerUtil {

  implicit val dateTimeJsReader: Reads[DateTime] = JodaReads.jodaDateReads("yyyyMMddHHmmss")
  implicit val dateTimeWriter: Writes[DateTime] = JodaWrites.jodaDateWrites("dd/MM/yyyy HH:mm:ss")

  def extendedAudit(auditModel: AuditModel, path: String = "-")(implicit hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    val extendedDataEvent = toExtendedDataEvent(appConfig.appName, auditModel, path)
    logger.debug(s"Splunk Audit Event:\n\n$extendedDataEvent")
    handleAuditResult(auditConnector.sendExtendedEvent(extendedDataEvent))
  }

  def toExtendedDataEvent(appName: String, auditModel: AuditModel, path: String)(implicit hc: HeaderCarrier): ExtendedDataEvent = {

    ExtendedDataEvent(
      auditSource = appName,
      auditType = auditModel.auditType,
      tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(auditModel.transactionName, path),
      detail = auditModel.detail
    )
  }

  private def handleAuditResult(auditResult: Future[AuditResult])(implicit ec: ExecutionContext): Unit = auditResult.map {
    case Success =>
      logger.debug("Splunk Audit Successful")
    case Failure(err, _) =>
      logger.debug(s"Splunk Audit Error, message: $err")
    case Disabled =>
      logger.debug(s"Auditing Disabled")
  }
}
