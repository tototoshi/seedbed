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

import scala.collection.mutable.{ Map => MutableMap, Set => MutableSet }
import java.sql._
import Control._

trait Seedbed { self: Configuration =>

  private val blueprints = MutableMap.empty[String, Blueprint]

  def define(name: String, defaults: Map[String, Any] = Map.empty): Unit = {
    withConnection(getConnection) { conn =>
      blueprints += (name -> Blueprint(name, Table(conn, name), defaults))
    }
  }

  private def getConnection: Connection = DBUtil.getConnection(this)

  def create(name: String, overrides: Map[String, Any] = Map.empty): Map[String, Any] = {
    val blueprint: Blueprint = blueprints.getOrElse(name, throw new DefinitionNotFoundException(s"$name is not defined"))

    val columnAndValues = blueprint.tableInfo.columnDefaultBase ++ blueprint.defaults ++ overrides

    val (columns, values) = columnMapToColumnAndValues(columnAndValues)
    val placeholders = values.map(_ => "?")

    val sql = s"""INSERT INTO $name (${columns.mkString(",")}) VALUES (${placeholders.mkString(",")})"""

    withConnection(getConnection) {
      conn =>
        val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        DBUtil.bindValue(values, stmt)
        stmt.executeUpdate()

        val generatedKeys =
          DBUtil.getGeneratedKey(stmt, blueprint.tableInfo.primaryKeys)

        val table = blueprint.tableInfo
        if (table.hasAutoIncrementColumn) {
          val autoIncrementColumns = table.autoIncrementColumns.map { c =>
            c.name -> generatedKeys(c.name)
          }
          get(name, autoIncrementColumns.toMap).getOrElse(throw new PersistenceException(s"INSERT to $name failed"))
        } else {
          columnAndValues
        }

    }
  }

  def clear(name: String): Unit = {
    withConnection(getConnection) { conn =>
      blueprints.get(name).foreach { bp =>
        DBUtil.deleteAll(conn, bp.name)
      }
    }
  }

  def clearAll(): Unit = {
    withConnection(getConnection) {
      conn =>
        blueprints.keys.foreach {
          table => DBUtil.deleteAll(conn, table)
        }
    }
  }

  def list(name: String, columns: Map[String, Any] = Map.empty): Seq[Map[String, Any]] = {
    val blueprint = blueprints.getOrElse(name, throw new DefinitionNotFoundException(s"$name is not defined"))
    val (cols, values) = columnMapToColumnAndValues(columns)

    withConnection(getConnection) {
      conn =>
        val conditions = DBUtil.createWhereCondition(columns, cols)
        val sql = s"SELECT * FROM $name " + conditions
        val stmt = conn.prepareStatement(sql)
        DBUtil.bindValue(values, stmt)
        val rs2 = stmt.executeQuery()
        var results: Seq[Map[String, Any]] = Seq.empty
        while (rs2.next()) {
          results = results :+ mapRow(blueprint, rs2)
        }
        results
    }
  }

  def get(name: String, columns: Map[String, Any]): Option[Map[String, Any]] = {
    val blueprint = blueprints.getOrElse(name, throw new DefinitionNotFoundException(s"$name is not defined"))
    val (cols, values) = columnMapToColumnAndValues(columns)
    withConnection(getConnection) {
      conn =>
        val conditions = DBUtil.createWhereCondition(columns, cols)
        val sql = s"SELECT * FROM $name " + conditions
        val stmt = conn.prepareStatement(sql)
        DBUtil.bindValue(values, stmt)
        val rs = stmt.executeQuery()
        if (!rs.next()) None else Some(mapRow(blueprint, rs))
    }
  }

  private def columnMapToColumnAndValues(m: Map[String, Any]): (Seq[String], Seq[Any]) = {
    val columnAndValues: List[(String, Any)] = m.toList.sorted(new Ordering[(String, Any)] {
      def compare(x: (String, Any), y: (String, Any)): Int = x._1.compareTo(y._1)
    })
    val columns = columnAndValues.map(_._1)
    val values = columnAndValues.map(_._2)
    (columns, values)
  }

  private[this] def mapRow(blueprint: Blueprint, rs: ResultSet): Map[String, Any] = {
    blueprint.tableInfo.columns.foldLeft(Map.empty[String, Any]) {
      case (result, Column(colName, tpe, _, _, _)) =>
        val value = DBUtil.extract(rs, tpe, colName)
        if (value != null) result + (colName -> DBUtil.extract(rs, tpe, colName)) else result
    }
  }

}
