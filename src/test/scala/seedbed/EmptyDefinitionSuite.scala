/*
 * Copyright 2014 Toshiyuki Takahashi
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

package seedbed

import org.scalatest._
import java.text.SimpleDateFormat
import scala.Some

class EmptyDefinitionSuite extends fixture.FunSuite with ShouldMatchers with OptionValues with DBFunSuite {

  type FixtureParam = Seedbed

  protected def withFixture(test: OneArgTest): Outcome = {
    val s = new Seedbed with TestDBConfiguration
    try {
      s.define("beatles")
      s.define("album")
      test(s)
    } finally {
      s.clearAll()
    }
  }

  test("#create") { s =>
    s.create("beatles")
    s.list("beatles") should have size 1
    s.create("album")
    s.list("album") should have size 1
  }

  test("nullable column") { s =>
    s.create("beatles")
    s.list("beatles").head.get("middle_name") should be(None)
  }

  test("Override default value") { s =>
    val john = s.create("beatles", Map("first_name" -> "Paul"))
    john.get("first_name") should be(Some("Paul"))
    val withTheBeatles = s.create("album", Map(
      "id" -> 2,
      "name" -> "With the beatles",
      "released_date" -> new SimpleDateFormat("yyyy-MM-dd").parse("1963-11-22"))
    )
    withTheBeatles.get("name") should be(Some("With the beatles"))
  }

}
