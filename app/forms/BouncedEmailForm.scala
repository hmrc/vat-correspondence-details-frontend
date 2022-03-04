/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.data.Forms._
import play.api.data.{Form, FormError}
import play.api.data.format.Formatter

object BouncedEmailForm {

  val verifyAdd: String = "verify_add"

  val verify: String = "verify"

  val add: String = "add"

  private val formatter: Formatter[Option[String]] = new Formatter[Option[String]] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      data.get(key) match {
        case Some("verify") => Right(Some("verify"))
        case Some("add") => Right(Some("add"))
        case _ => Left(Seq(FormError(key, "bouncedEmail.formError")))
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = {
      val stringValue = value match {
        case Some("verify") => "verify"
        case Some("add") => "add"
      }
      Map(key -> stringValue)
    }
  }

  def bouncedEmailForm: Form[Option[String]] = Form(
    single(
      verifyAdd -> of(formatter)
    )
  )

}
