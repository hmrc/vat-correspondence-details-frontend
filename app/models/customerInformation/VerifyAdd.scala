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

package models.customerInformation

import play.api.libs.json._

trait VerifyAdd {
  val value: String
}

object VerifyAdd {
  val id: String = "verifyAdd"

  implicit val reads: Reads[VerifyAdd] = for {
    verifyAddOption <- (__ \ id).read[String].map{
      case Verify.value => Verify
      case Add.value => Add
    }
  } yield verifyAddOption
}

object Verify extends VerifyAdd {
  override val value = "verify"
}

object Add extends VerifyAdd {
  override val value = "add"
}
