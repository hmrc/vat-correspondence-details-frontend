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

package schemaTest

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchemaFactory
import org.scalatest.matchers.should.Matchers
import utils.TestUtil

class JsonValidationTest extends TestUtil with Matchers {

  case class Schema(schemaPath: String) {
    private val validator = JsonSchemaFactory.byDefault().getJsonSchema(schemaPath)

    def validate(json: String): ProcessingReport =
      validator.validate(new ObjectMapper().readTree(json), true)
  }

  def getUriString(name: String): String =
    getClass.getResource(name).toURI.toString


 val schemaPath = getUriString("schemaTest/vatUpdateSubscriptionSuccessSchema.json")

  def bodyIsOfSchema(schemaPath: String, body: String): Unit = {
    val report = Schema(schemaPath).validate(body)

    withClue(report.toString) {
      report.isSuccess shouldBe true
    }
  }

  val testBody =
    """
      |
      |""".stripMargin

  "JSON data" should {
    "validate against the JSON schema" in {
      bodyIsOfSchema(schemaPath, testBody)
    }
  }
}
