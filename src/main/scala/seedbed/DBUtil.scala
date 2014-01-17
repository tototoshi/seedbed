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

import java.sql._
import scala.collection.mutable.{ Map => MutableMap, Set => MutableSet }
import seedbed.Column

private[seedbed] object DBUtil {

  def getConnection(configuration: Configuration): Connection = {
    Class.forName(configuration.driver)
    DriverManager.getConnection(configuration.url, configuration.user, configuration.password)
  }

  def getColumns(conn: Connection, name: String): Map[String, Column] = {
    val columns: MutableMap[String, Column] = MutableMap.empty[String, Column]
    val rs = conn.getMetaData.getColumns(null, null, name, "%")
    while (rs.next()) {
      val columnName = rs.getString("COLUMN_NAME")
      val dataType = rs.getInt("DATA_TYPE")
      val nullable = rs.getInt("NULLABLE") == 1
      val autoIncrement = rs.getString("IS_AUTOINCREMENT") == "YES"
      columns += (columnName -> Column(columnName, dataType, isPrimaryKey = false, autoIncrement, nullable))
    }

    val primaryKeys = getPrimaryKeys(conn, name)
    primaryKeys.foreach { pk =>
      columns += (pk -> columns(pk).copy(isPrimaryKey = true))
    }

    columns.toMap
  }

  def getAutoIncrementColumns(conn: Connection, name: String): Map[String, Int] = {
    val columns: MutableMap[String, Int] = MutableMap.empty[String, Int]
    val rs = conn.prepareStatement("SELECT * FROM " + name).executeQuery()
    val meta = rs.getMetaData
    for (columnIndex <- 1 to meta.getColumnCount) {
      val meta = rs.getMetaData
      if (meta.isAutoIncrement(columnIndex)) {
        val columnName = meta.getColumnName(columnIndex)
        columns += (columnName -> meta.getColumnType(columnIndex))
      }
    }
    columns.toMap
  }

  def getPrimaryKeys(conn: Connection, name: String): Set[String] = {
    val primaryKeys: MutableSet[String] = MutableSet.empty[String]
    val rs = conn.getMetaData.getPrimaryKeys(null, null, name)
    while (rs.next()) {
      primaryKeys += rs.getString("COLUMN_NAME")
    }
    primaryKeys.toSet
  }

  def getGeneratedKey(stmt: PreparedStatement, columns: Set[Column]): Map[String, Any] = {
    val generatedKey: MutableMap[String, Any] = MutableMap.empty
    val rs = stmt.getGeneratedKeys
    while (rs.next()) {
      columns.foreach { col =>
        generatedKey += col.name -> extract(rs, col.jdbcType, col.name)
      }
    }
    generatedKey.toMap
  }

  def bindValue(values: Seq[Any], stmt: PreparedStatement) {

    def millisToSqlType(d: { def getTime(): Long }): java.sql.Date = {
      import java.util.Calendar
      val cal = Calendar.getInstance()
      cal.setTimeInMillis(d.getTime)
      cal.set(Calendar.HOUR_OF_DAY, 0)
      cal.set(Calendar.MINUTE, 0)
      cal.set(Calendar.SECOND, 0)
      cal.set(Calendar.MILLISECOND, 0)
      new java.sql.Date(cal.getTimeInMillis)
    }

    var index: Int = 1
    values.foreach {
      v =>
        v match {
          case v: Int => stmt.setInt(index, v)
          case v: Short => stmt.setShort(index, v)
          case v: Long => stmt.setLong(index, v)
          case v: Float => stmt.setFloat(index, v)
          case v: Double => stmt.setDouble(index, v)
          case v: String => stmt.setString(index, v)
          case v: java.math.BigDecimal => stmt.setBigDecimal(index, v)
          case v: java.sql.Time => stmt.setTime(index, v)
          case v: java.sql.Timestamp => stmt.setTimestamp(index, v)
          case v: java.sql.Date => stmt.setDate(index, v)
          case v: java.util.Date => stmt.setTimestamp(index, new Timestamp(v.getTime))
          case _ => new UnsupportedTypeException("Not supported type")
        }
        index += 1
    }
  }

  def deleteAll(conn: Connection, table: String): Int = {
    conn.prepareStatement(s"DELETE FROM $table").executeUpdate()
  }

  def createWhereCondition(columns: Map[String, Any], cols: Seq[String]): String = {
    if (columns.isEmpty) ""
    else cols.map { c => s"$c = ?" }.mkString("WHERE ", " AND ", "")
  }

  def extract(rs: ResultSet, tpe: Int, colName: String): Any = {
    tpe match {
      case java.sql.Types.ARRAY => rs.getArray(colName)
      case java.sql.Types.BIGINT => rs.getLong(colName)
      case java.sql.Types.BINARY => rs.getBytes(colName)
      case java.sql.Types.BIT => rs.getByte(colName)
      case java.sql.Types.BLOB => rs.getBlob(colName)
      case java.sql.Types.BOOLEAN => rs.getBoolean(colName)
      case java.sql.Types.CHAR => rs.getString(colName)
      case java.sql.Types.CLOB => rs.getClob(colName)
      case java.sql.Types.DATALINK => new UnsupportedTypeException("not supported")
      case java.sql.Types.DATE => rs.getDate(colName)
      case java.sql.Types.DECIMAL => rs.getBigDecimal(colName)
      case java.sql.Types.DISTINCT => new UnsupportedTypeException("not supported")
      case java.sql.Types.DOUBLE => rs.getDouble(colName)
      case java.sql.Types.FLOAT => rs.getFloat(colName)
      case java.sql.Types.INTEGER => rs.getInt(colName)
      case java.sql.Types.JAVA_OBJECT => rs.getObject(colName)
      case java.sql.Types.LONGNVARCHAR => rs.getString(colName)
      case java.sql.Types.LONGVARBINARY => rs.getBytes(colName)
      case java.sql.Types.LONGVARCHAR => rs.getString(colName)
      case java.sql.Types.NCHAR => rs.getString(colName)
      case java.sql.Types.NCLOB => rs.getClob(colName)
      case java.sql.Types.NULL => null
      case java.sql.Types.NUMERIC => rs.getInt(colName)
      case java.sql.Types.NVARCHAR => rs.getString(colName)
      case java.sql.Types.OTHER => new UnsupportedTypeException("not supported")
      case java.sql.Types.REAL => new UnsupportedTypeException("not supported")
      case java.sql.Types.REF => new UnsupportedTypeException("not supported")
      case java.sql.Types.ROWID => new UnsupportedTypeException("not supported")
      case java.sql.Types.SMALLINT => rs.getInt(colName)
      case java.sql.Types.SQLXML => new UnsupportedTypeException("not supported")
      case java.sql.Types.STRUCT => new UnsupportedTypeException("not supported")
      case java.sql.Types.TIME => rs.getTime(colName)
      case java.sql.Types.TIMESTAMP => rs.getTimestamp(colName)
      case java.sql.Types.TINYINT => rs.getInt(colName)
      case java.sql.Types.VARBINARY => rs.getBytes(colName)
      case java.sql.Types.VARCHAR => rs.getString(colName)
      case _ => new UnsupportedTypeException("Unknown type")
    }
  }

}
