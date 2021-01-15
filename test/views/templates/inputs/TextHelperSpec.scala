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

package views.templates.inputs

import play.api.data.{Field, FormError}
import play.twirl.api.Html
import forms.FeatureSwitchForm
import views.ViewBaseSpec
import views.html.templates.inputs.Text

class TextHelperSpec extends ViewBaseSpec {

  val injectedView: Text = inject[Text]

  val fieldName = "fieldName"
  val title = "Title"
  val field: Field = Field(FeatureSwitchForm.form, fieldName, Seq(), None, Seq(), Some("text"))

  "The Text input form helper" when {

    "all parameters are default and there are no errors" should {

      "render the correct HTML" in {

        val expectedMarkup = Html(
          s"""
             |<div class="form-group">
             |  <fieldset aria-describedby="form-hint">
             |    <div class="form-field">
             |      <h1 id="page-heading"><label for="$fieldName" class="heading-large">$title</label></h1>
             |      <input class="form-control input--no-spinner" name="$fieldName" id="$fieldName" value="text">
             |    </div>
             |  </fieldset>
             |</div>
             |""".stripMargin
        )

        val result = injectedView(field, Some(title))

        formatHtml(result) shouldBe formatHtml(expectedMarkup)
      }
    }

    "there is an error" should {

      "render the error span and apply the form-field--error class to the field" in {

        val errorField = Field(FeatureSwitchForm.form, fieldName, Seq(), None, Seq(FormError("error", "ERROR")), Some("text"))

        val expectedMarkup = Html(
          s"""
             |<div class="form-group">
             |  <fieldset aria-describedby="form-hint form-error">
             |    <div class="form-field--error panel-border-narrow">
             |      <h1 id="page-heading"><label for="$fieldName" class="heading-large">$title</label></h1>
             |      <span id="form-error" class="error-message">
             |        <span class="visuallyhidden">Error:</span>
             |        ERROR
             |      </span>
             |      <input class="form-control input--no-spinner" name="$fieldName" id="$fieldName" value="text">
             |    </div>
             |  </fieldset>
             |</div>
             |""".stripMargin
        )

        val result = injectedView(errorField, Some(title))

        formatHtml(result) shouldBe formatHtml(expectedMarkup)
      }
    }

    "there is some additional content" should {

      "render the provided content inside of the form" in {

        val expectedMarkup = Html(
          s"""
             |<div class="form-group">
             |  <fieldset aria-describedby="form-hint">
             |    <div class="form-field">
             |      <h1 id="page-heading"><label for="$fieldName" class="heading-large">$title</label></h1>
             |      <p>Additional HTML</p>
             |      <input class="form-control input--no-spinner" name="$fieldName" id="$fieldName" value="text">
             |    </div>
             |  </fieldset>
             |</div>
             |""".stripMargin
        )

        val result = injectedView(field, Some(title), additionalContent = Some(Html("<p>Additional HTML</p>")))

        formatHtml(result) shouldBe formatHtml(expectedMarkup)
      }
    }

    "there is no page title" should {

      "render the provided content inside of the form" in {

        val expectedMarkup = Html(
          s"""
             |<div class="form-group">
             |  <fieldset aria-describedby="form-hint">
             |    <div class="form-field">
             |      <input class="form-control input--no-spinner" name="$fieldName" id="$fieldName" value="text">
             |    </div>
             |  </fieldset>
             |</div>
             |""".stripMargin
        )

        val result = injectedView(field, None)

        formatHtml(result) shouldBe formatHtml(expectedMarkup)
      }
    }
  }
}
