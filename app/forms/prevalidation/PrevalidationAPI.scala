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

import play.api.data.Form

object TrimOption extends Enumeration {
  type TrimOption = Value
  val both, all, bothAndCompress, none = Value
}

object CaseOption extends Enumeration {
  type CaseOption = Value
  val upper, lower, none = Value
}

trait PrevalidationAPI[T] {

  import CaseOption._
  import TrimOption._

  type PreprocessFunction = Map[String, String] => Map[String, String]

  val form: Form[T]
  val trimRules: Map[String, TrimOption]
  val caseRules: Map[String, CaseOption]

  protected val preprocessFunctions: Seq[PreprocessFunction] = Seq()

  private def removeKeyPrefix(key: String): String =
    key.lastIndexOf(".") match {
      case -1 => key
      case v => key.substring(v + 1)
    }

  def addNewPreprocessFunction(preprocessFunction: PreprocessFunction): PrevalidationAPI[T] = {
    val fValidation = form
    val trules = trimRules
    val crules = caseRules
    val newPreprocessFunctions = preprocessFunctions :+ preprocessFunction

    new PrevalidationAPI[T] {
      override val form: Form[T] = fValidation
      override val trimRules: Map[String, TrimOption] = trules
      override val caseRules: Map[String, CaseOption] = crules
      override val preprocessFunctions: Seq[PreprocessFunction] = newPreprocessFunctions
    }
  }

  private def preProcessFormData(data: Map[String, String]): Map[String, String] =
    preprocessFunctions.foldLeft(data)((data, function) => function(data))

  private def preprocess(key: String, value: String): String = {
    val trimmedField: String = trimRules.getOrElse(removeKeyPrefix(key), TrimOption.bothAndCompress) match {
      case TrimOption.both => trimBothFunc(value)
      case TrimOption.bothAndCompress => trimBothAndCompressFunc(value)
      case TrimOption.all => trimAllFunc(value)
      case TrimOption.none => value
    }

    val sanitisedField: String = XssFilter.filter(trimmedField)
    caseRules.getOrElse(removeKeyPrefix(key), CaseOption.none) match {
      case CaseOption.upper => sanitisedField.toUpperCase
      case CaseOption.lower => sanitisedField.toLowerCase
      case CaseOption.none => sanitisedField
    }
  }

  private def cleanRequestForm(data: Map[String, Seq[String]]): Map[String, Seq[String]] =
    data.map {
      case (key, values) =>
        (key, values.map(preprocess(key, _)))
    }

  private def cleanForm(data: Map[String, String]): Map[String, String] =
    data.map { case (key, value) => (key, preprocess(key, value)) }

  def bind(data: Map[String, String]): Form[T] =
    form.bind(preProcessFormData(cleanForm(data)))

  private def bindFromRequest(data: Map[String, Seq[String]]): Form[T] =
    form.bind(preProcessFormData(cleanRequestForm(data)))

  //$COVERAGE-OFF$Disabling scoverage on these methods since they are copied from play's source

  // copied the source from play 2.5
  // https://github.com/playframework/playframework/blob/master/framework/src/play/src/main/scala/play/api/data/Form.scala
  // used to convert a Request object into Map[String, Seq[String]] then call our trimmed
  // bindFromRequest function
  def bindFromRequest()(implicit request: play.api.mvc.Request[_]): Form[T] = {
    bindFromRequest {
      (request.body match {
        case body: play.api.mvc.AnyContent if body.asFormUrlEncoded.isDefined => body.asFormUrlEncoded.get
        case body: play.api.mvc.AnyContent if body.asMultipartFormData.isDefined => body.asMultipartFormData.get.asFormUrlEncoded
        case body: play.api.mvc.AnyContent if body.asJson.isDefined => FormUtils.fromJson(js = body.asJson.get).mapValues(Seq(_))
        case body: Map[_, _] => body.asInstanceOf[Map[String, Seq[String]]]
        case body: play.api.mvc.MultipartFormData[_] => body.asFormUrlEncoded
        case body: play.api.libs.json.JsValue => FormUtils.fromJson(js = body).mapValues(Seq(_))
        case _ => Map.empty[String, Seq[String]]
      }) ++ request.queryString
    }
  }


  // extracted from the source of play 2.5
  // https://github.com/playframework/playframework/blob/master/framework/src/play/src/main/scala/play/api/data/Form.scala
  // ( def bindFromRequest(data: Map[String, Seq[String]]): Form[T] )
  implicit def conv(data: Map[String, Seq[String]]): Map[String, String] =
  data.foldLeft(Map.empty[String, String]) {
    case (s, (key, values)) if key.endsWith("[]") => s ++ values.zipWithIndex.map { case (v, i) => (key.dropRight(2) + "[" + i + "]") -> v }
    case (s, (key, values)) => s + (key -> values.headOption.getOrElse(""))
  }

  // $COVERAGE-ON$
}

//$COVERAGE-OFF$Disabling scoverage on this since it is copied from play's source

// copied the source from play 2.5
// https://github.com/playframework/playframework/blob/master/framework/src/play/src/main/scala/play/api/data/Form.scala
// this is used by bindFromRequest
private object FormUtils {

  import play.api.libs.json._

  def fromJson(prefix: String = "", js: JsValue): Map[String, String] = js match {
    case JsObject(fields) => {
      fields.map { case (key, value) => fromJson(Option(prefix).filterNot(_.isEmpty).map(_ + ".").getOrElse("") + key, value) }.foldLeft(Map.empty[String, String])(_ ++ _)
    }
    case JsArray(values) => {
      values.zipWithIndex.map { case (value, i) => fromJson(prefix + "[" + i + "]", value) }.foldLeft(Map.empty[String, String])(_ ++ _)
    }
    case JsNull => Map.empty
    case JsUndefined() => Map.empty
    case JsBoolean(value) => Map(prefix -> value.toString)
    case JsNumber(value) => Map(prefix -> value.toString)
    case JsString(value) => Map(prefix -> value.toString)
  }
}

// $COVERAGE-ON$
