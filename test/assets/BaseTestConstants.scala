/*
 * Copyright 2024 HM Revenue & Customs
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

package assets

import common.EnrolmentKeys
import models.errors.ErrorModel
import uk.gov.hmrc.auth.core.Enrolment

object BaseTestConstants {

  val arn = "ABCD12345678901"
  val vrn: String = "999999999"
  val testMtdVatEnrolment: Enrolment = Enrolment(EnrolmentKeys.vatEnrolmentId).withIdentifier(EnrolmentKeys.vatIdentifierId, vrn)
  val formBundle = "XA1234567"
  val internalServerErrorTitle = "There is a problem with the service - Manage your VAT account - GOV.UK"
  val errorModel: ErrorModel = ErrorModel(1, "Error")

  val testValidationLandline: String = "01952123456"
  val testPrepopLandline: String = "01952654321"
  val testValidationMobile: String = "07890123456"
  val testPrepopMobile: String = "07890654321"

  val testWebsite: String = "www.test.com"
  val testNewWebsite: String = "www.test2.com"
}
