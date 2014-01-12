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
import java.sql.Connection

class TableSuite extends fixture.FunSuite with DBFunSuite with ShouldMatchers with OptionValues {

  type FixtureParam = Connection

  protected def withFixture(test: OneArgTest): Outcome = {
    val conn = DBUtil.getConnection(TestDBConfiguration)
    Control.withConnection(conn)(test)
  }

  test("construction") { conn =>
    Table(conn, "beatles").columns should have size 5
    Table(conn, "album").columns should have size 3
  }

  test("#columnDefaultBase") { conn =>
    val default = Table(conn, "beatles").columnDefaultBase
    default.get("id") should be(None)
    default.get("first_name") should be(Some(""))
    default.get("middle_name") should be(None)
  }

}
