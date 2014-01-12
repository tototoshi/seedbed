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
import java.sql.Connection

class DBUtilSuite extends fixture.FunSuite with DBFunSuite with ShouldMatchers with OptionValues {

  type FixtureParam = Connection

  protected def withFixture(test: OneArgTest): Outcome = {
    Control.withConnection(DBUtil.getConnection(TestDBConfiguration))(test)
  }

  test("#getColumns") { conn =>
    val columns = DBUtil.getColumns(conn, "beatles")
    columns should not be empty
    columns("id") should be(Column("id", 4, isPrimaryKey = true, isAutoIncrement = true, isNullable = false))
  }

  test("#getAutoInclementColumns") { conn =>
    val columns = DBUtil.getAutoIncrementColumns(conn, "beatles")
    columns should have size 1
    columns("id") should be(java.sql.Types.INTEGER)

    DBUtil.getAutoIncrementColumns(conn, "album") should be(empty)
  }

  test("#getPrimaryKeys") { conn =>
    DBUtil.getPrimaryKeys(conn, "beatles") should be(Set("id"))
    DBUtil.getPrimaryKeys(conn, "album") should be(Set("id"))
  }

}

