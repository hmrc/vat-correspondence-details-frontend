/*
 * Copyright 2024 HM Revenue & Customs
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

package config.filters

import org.apache.pekko.util.ByteString

import javax.inject.Inject
import play.api.libs.streams.Accumulator
import play.api.mvc._
import play.api.routing.Router
import play.filters.csrf._

/*
This allow a routes be labeled in the route file to exclude a csrf check,
 see https://dominikdorn.com/2014/07/playframework-2-3-global-csrf-protection-disable-csrf-selectively/
  e.g.

 # NOCSRF
 /my-route   controllers.routes.NoCSRFCheckController.post()

 */
class ExcludingCSRFFilter @Inject()(filter: CSRFFilter) extends EssentialFilter {

  override def apply(nextFilter: EssentialAction): EssentialAction = new EssentialAction {

    override def apply(rh: RequestHeader): Accumulator[ByteString, Result] = {
      val chainedFilter = filter.apply(nextFilter)
      rh.attrs.get(Router.Attrs.HandlerDef).fold {
        chainedFilter(rh)
      } { handler =>
        if (handler.comments.contains("NOCSRF")) {
          nextFilter(rh)
        } else {
          chainedFilter(rh)
        }
      }
    }
  }
}
