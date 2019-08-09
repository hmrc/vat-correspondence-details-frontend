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

package forms

import com.google.i18n.phonenumbers.PhoneNumberUtil
import models.customerInformation.ContactNumbers
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import uk.gov.hmrc.play.mappers.StopOnFirstFail
import uk.gov.hmrc.play.mappers.StopOnFirstFail.constraint
import utils.LoggerUtil.logWarn

object ContactNumbersForm {

  private val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

  def contactNumbersForm(currentLandline: String, currentMobile: String): Form[ContactNumbers] = Form(
    mapping(
        "landlineNumber" -> optional(text).verifying(
            StopOnFirstFail(
            constraint[Option[String]]("captureContactNumbers.error.empty", _.forall(_.length != 0)),
            constraint[Option[String]]("captureContactNumbers.error.invalid", _.fold(true)(phoneNumberFormatIsValid))
          )
        ),
        "mobileNumber" -> optional(text).verifying(
          StopOnFirstFail(
            constraint[Option[String]]("captureContactNumbers.error.empty", _.forall(_.length != 0)),
            constraint[Option[String]]("captureContactNumbers.error.invalid", _.fold(true)(phoneNumberFormatIsValid))
          )
        )
    )(ContactNumbers.apply)(ContactNumbers.unapply).verifying(contactNumbersConstraint)
  )

  private def phoneNumberFormatIsValid(phoneNumber: String): Boolean = {
    try {
      val defaultRegion = if (!phoneNumber.startsWith("+")) "GB" else ""
      phoneNumberUtil.isValidNumber(phoneNumberUtil.parse(phoneNumber, defaultRegion))
    } catch {
      case e: Exception =>
        logWarn("[ContactNumbersForm][phoneNumberFormatIsValid] - Couldn't parse provided phone number")
        false
    }
  }

  private val contactNumbersConstraint: Constraint[ContactNumbers] = Constraint("constraints.contactNumbersCheck")({
    form =>
      if (form.landlineNumber.isDefined || form.mobileNumber.isDefined) {
        Valid
      } else {
        Valid
        Invalid(Seq(ValidationError("captureContactNumbers.error.noEntry")))
      }
  })
}
