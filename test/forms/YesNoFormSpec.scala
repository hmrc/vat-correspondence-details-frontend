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

package forms

import forms.YesNoForm._
import models.{No, Yes}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class YesNoFormSpec extends AnyWordSpecLike with Matchers {

  "YesNoForm" should {

    "successfully parse a Yes" in {
      val res = yesNoForm("empty").bind(Map(yesNo -> YesNoForm.yes))
      res.value should contain(Yes)
    }

    "successfully parse a No" in {
      val res = yesNoForm("empty").bind(Map(yesNo -> YesNoForm.no))
      res.value should contain(No)
    }

  }

  "Binding a form with invalid data" when {

    "no option has been selected" should {

      val missingOption: Map[String, String] = Map.empty
      val form = YesNoForm.yesNoForm("empty").bind(missingOption)

      "result in a form with errors" in {
        form.hasErrors shouldBe true
      }

      "have the Select and Option error" in {
        form.errors.head.message shouldBe "empty"
      }
    }
  }
}