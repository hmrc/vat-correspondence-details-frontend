/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraint

import forms.prevalidation.PreprocessedForm
import forms.prevalidation.CaseOption._
import forms.prevalidation.TrimOption._
import forms.prevalidation.PrevalidationAPI
import forms.validation.utils.ConstraintUtil._
import forms.validation.utils.MappingUtil._
import forms.validation.utils.ValidationHelper._

object EmailForm {

  val email      = "email"
  val maxLength  = 132
  val emailRegex = """^(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\
                   |x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[
                   |a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4]
                   |[0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\
                   |x09\x0b\x0c\x0e-\x7f])+)\])$""".stripMargin

  val emailExceedsMaxLength: Constraint[String] = Constraint("email.maxLength")(
    email => validate(
      constraint = email.trim.length > maxLength,
      message = "error.exceeds_max_length_email"
    )
  )

  val emailInvalid: Constraint[String] = Constraint("email.invalid")(
    email => validateNot(
      constraint = email matches emailRegex,
      message = "error.invalid_email"
    )
  )

  def emailNotEntered: Constraint[String] = Constraint("email.not_entered")(
    email => validate(
      constraint = email.isEmpty,
      message = "error.empty_email"
    )
  )

  private def emailValidationForm = Form(
    single(
      email -> optText.toText.verifying(emailNotEntered andThen emailExceedsMaxLength andThen emailInvalid)
    )
  )

  def emailForm: PrevalidationAPI[String] = PreprocessedForm(
    validation = emailValidationForm,
    trimRules = Map(email -> all),
    caseRules = Map(email -> lower)
  )
}
