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

object TestHelper {

  def createTables(): Unit = {
    val conn = DBUtil.getConnection(TestDBConfiguration)

    val sql = if (TestDBConfiguration.url.contains("jdbc:h2:")) {
      """
        |CREATE TABLE beatles (
        |  id integer PRIMARY KEY AUTO_INCREMENT NOT NULL,
        |  first_name varchar(10) NOT NULL,
        |  middle_name varchar(10),
        |  last_name varchar(10) NOT NULL,
        |  birth_date date not null
        |);
        |
        |CREATE TABLE album (
        |  id integer primary key NOT NULL,
        |  name varchar(30) NOT NULL,
        |  released_date date NOT NULL
        |);
      """.stripMargin
    } else {
      """
        |CREATE TABLE beatles (
        |  id serial primary key,
        |  first_name varchar(10) NOT NULL,
        |  middle_name varchar(10),
        |  last_name varchar(10) NOT NULL,
        |  birth_date date not null
        |);
        |
        |CREATE TABLE album (
        |  id integer primary key,
        |  name varchar(30) NOT NULL,
        |  released_date date NOT NULL
        |);
      """.stripMargin
    }
    val stmt = conn.prepareStatement(sql)
    stmt.execute()
  }

  def dropTables(): Unit = {
    val conn = DBUtil.getConnection(TestDBConfiguration)
    val sql =
      """
        |DROP TABLE beatles;
        |DROP TABLE album;
      """.stripMargin
    val stmt = conn.prepareStatement(sql)
    stmt.execute()
  }

}
