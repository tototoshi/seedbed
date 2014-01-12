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

import java.sql.{ Timestamp, Time, Connection }

case class Table(name: String, columns: Set[Column]) {

  def columnNames: Set[String] = columns.map(_.name)

  def primaryKeys: Set[Column] = columns.filter(_.isPrimaryKey)

  def autoIncrementColumns: Set[Column] = columns.filter(_.isAutoIncrement)

  def hasAutoIncrementColumn: Boolean = columns.exists(_.isAutoIncrement)

  def nullableColumns: Set[Column] = columns.filter(_.isNullable)

  def columnDefaultBase: Map[String, Any] = {
    def columnDefault(tpe: Int): Any = {
      import java.sql.Types._
      tpe match {
        case ARRAY => scala.Array()
        case BIGINT => 0
        case BINARY | VARBINARY | LONGVARBINARY => scala.Array()
        case BIT => Byte.MinValue
        case BLOB => new UnsupportedTypeException("not supported")
        case BOOLEAN => false
        case CHAR | NCHAR => ' '
        case CLOB | NCLOB => new UnsupportedTypeException("not supported")
        case DATALINK => new UnsupportedTypeException("not supported")
        case DATE => new java.util.Date()
        case DECIMAL => java.math.BigDecimal.ZERO
        case DISTINCT => new UnsupportedTypeException("not supported")
        case DOUBLE => 0d
        case FLOAT => 0f
        case INTEGER | SMALLINT | TINYINT | NUMERIC => 0
        case JAVA_OBJECT => new AnyRef {}
        case NULL => null
        case OTHER => new UnsupportedTypeException("not supported")
        case REAL => new UnsupportedTypeException("not supported")
        case REF => new UnsupportedTypeException("not supported")
        case ROWID => new UnsupportedTypeException("not supported")
        case SQLXML => new UnsupportedTypeException("not supported")
        case STRUCT => new UnsupportedTypeException("not supported")
        case TIME => new Time(new java.util.Date().getTime)
        case TIMESTAMP => new Timestamp(new java.util.Date().getTime)
        case VARCHAR | NVARCHAR | LONGNVARCHAR | LONGVARCHAR => ""
        case _ => new UnsupportedTypeException("Unknown type")
      }
    }

    (for {
      c <- columns
      if !c.isAutoIncrement
      if !c.isNullable
    } yield {
      c.name -> columnDefault(c.jdbcType)
    }).toMap
  }

}

object Table {

  private[seedbed] def apply(conn: Connection, tableName: String): Table = {
    Table(tableName, DBUtil.getColumns(conn, tableName).values.toSet)
  }

}
