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

package models

import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments, InternalError}

case class Agent(arn: String)

object Agent {
  def apply[A](enrolments: Enrolments): Agent =
    enrolments.enrolments.collectFirst {
      case Enrolment("HMRC-AS-AGENT", EnrolmentIdentifier(_, arn) :: _, _, _) => Agent(arn)
    }.getOrElse(throw InternalError("Agent Service Enrolment Missing"))
}
