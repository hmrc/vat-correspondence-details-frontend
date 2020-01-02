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

import assets.CustomerInfoConstants._
import connectors.VatSubscriptionConnector
import connectors.httpParsers.GetCustomerInfoHttpParser.GetCustomerInfoResponse
import connectors.httpParsers.UpdatePPOBHttpParser.UpdatePPOBResponse
import models.User
import models.customerInformation.{PPOB, UpdatePPOBSuccess}
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockVatSubscriptionConnector extends MockFactory {

  val connector: VatSubscriptionConnector = mock[VatSubscriptionConnector]

  def mockGetCustomerInfoResponse(result: Future[GetCustomerInfoResponse]): Unit = {
    (connector.getCustomerInfo(_: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *)
      .returns(result)
  }

  def mockUpdatePPOBResponse(result: Future[UpdatePPOBResponse]): Unit = {
    (connector.updatePPOB(_: String, _: PPOB)(_: HeaderCarrier, _: ExecutionContext, _: User[_]))
      .expects(*, *, *, *, *)
      .returns(result)
  }

  def mockGetCustomerInfoSuccessResponse(): Unit = mockGetCustomerInfoResponse(Future.successful(Right(fullCustomerInfoModel)))
  def mockGetCustomerInfoFailureResponse(): Unit = mockGetCustomerInfoResponse(Future.successful(Left(invalidJsonError)))

  def mockUpdatePPOBSuccessResponse(): Unit = mockUpdatePPOBResponse(Future.successful(Right(UpdatePPOBSuccess("success"))))
  def mockUpdatePPOBFailureResponse(): Unit = mockUpdatePPOBResponse(Future.successful(Left(invalidJsonError)))
}
