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

class SeedbedSuite extends fixture.FunSuite with ShouldMatchers with OptionValues with DBFunSuite {

  type FixtureParam = Seedbed

  protected def withFixture(test: OneArgTest): Outcome = {
    val s = new Seedbed with TestDBConfiguration

    try {
      s.define("beatles", Map(
        "first_name" -> "John",
        "middle_name" -> "Winston",
        "last_name" -> "Lennon",
        "birth_date" -> new SimpleDateFormat("yyyy-MM-dd").parse("1940-10-09")
      ))

      s.define("album", Map(
        "id" -> 1,
        "name" -> "Please Please Me",
        "released_date" -> new SimpleDateFormat("yyyy-MM-dd").parse("1963-3-22")
      ))

      test(s)
    } finally {
      s.cleanAll()
    }
  }

  test("Define and create") { s =>
    val john = s.create("beatles")
    john.get("first_name") should be(Some("John"))
    val pleasePleaseMe = s.create("album")
    pleasePleaseMe.get("name") should be(Some("Please Please Me"))
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

  test("#clean") { s =>
    s.create("beatles")
    s.create("album")
    s.clean("beatles")
    s.list("beatles") should be(empty)
    s.list("album") should not be empty
  }

  test("#cleanAll") { s =>
    s.create("beatles")
    s.create("beatles")
    s.create("beatles")
    s.cleanAll()
    s.list("beatles") should be(empty)
  }

  test("#get") { s =>
    s.create("beatles")
    s.get("beatles", Map("first_name" -> "Paul")) should be(empty)
    s.create("beatles", Map("first_name" -> "Paul"))
    val p = s.get("beatles", Map("first_name" -> "Paul"))
    p.value.get("first_name") should be(Some("Paul"))
    p.value.get("middle_name") should be(Some("Winston"))
    p.value.get("last_name") should be(Some("Lennon"))
    p.value.get("birth_date").value.toString should be("1940-10-09")
  }

  test("#list") { s =>
    s.create("beatles")
    s.create("beatles", Map("first_name" -> "Paul"))
    s.create("beatles", Map("first_name" -> "Paul", "last_name" -> "McCartney"))
    s.list("beatles") should have size 3
    s.list("beatles", Map("first_name" -> "Paul")) should have size 2
    s.list("beatles", Map("first_name" -> "Paul", "last_name" -> "McCartney")) should have size 1
  }

}
