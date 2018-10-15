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

package forms.prevalidation

import java.util.regex.Pattern

trait XssFilter {

  def filter(input: String): String = {
    var value: String = input
    if (value != null) {
      // NOTE: It's highly recommended to use the ESAPI library and uncomment the following line to
      // avoid encoded attacks.
      // value = ESAPI.encoder().canonicalize(value)

      // Avoid null characters
      value = value.replaceAll("", "")

      // Avoid anything between script tags
      var scriptPattern: Pattern = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE)
      value = scriptPattern.matcher(value).replaceAll("")

      // Avoid anything in a src='...' type of expression
      scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
      value = scriptPattern.matcher(value).replaceAll("")

      scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
      value = scriptPattern.matcher(value).replaceAll("")

      // Remove any lonesome </script> tag
      scriptPattern = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE)
      value = scriptPattern.matcher(value).replaceAll("")

      // Remove any lonesome <script ...> tag
      scriptPattern = Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
      value = scriptPattern.matcher(value).replaceAll("")

      // Avoid eval(...) expressions
      scriptPattern = Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
      value = scriptPattern.matcher(value).replaceAll("")

      // Avoid expression(...) expressions
      scriptPattern = Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
      value = scriptPattern.matcher(value).replaceAll("")

      // Avoid javascript:... expressions
      scriptPattern = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE)
      value = scriptPattern.matcher(value).replaceAll("")

      // Avoid vbscript:... expressions
      scriptPattern = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE)
      value = scriptPattern.matcher(value).replaceAll("")

      // Avoid onload= expressions
      scriptPattern = Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
      value = scriptPattern.matcher(value).replaceAll("")
    }

    value
  }
}

object XssFilter extends XssFilter
