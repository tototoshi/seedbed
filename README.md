# Seedbed
An alternative to using database fixtures in your Scala unit tests.

This project is inspired by factory_girl or phactory.

## Overview

- so simple
- no other dependencies than jdbc

## Configuration

```scala
resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies += Seq(
  "com.github.tototoshi" %% "seedbed" % "0.1.0-SNAPSHOT",
  // and database driver
)
```

## Usage

First, you need to create an object for configuration by extending `Configuration` trait.

```scala
trait TestDBConfiguration extends Configuration {
  val driver: String = "org.postgresql.Driver"
  val url: String = "jdbc:postgresql://localhost/seedbed_test"
  val user: String = "user"
  val password: String = "password"
}
```

```scala
/*
Imagine that you have a table like this.
CREATE table beatles (
  id serial primary key,
  first_name varchar(10) not null,
  middle_name varchar(10),
  last_name varchar(10) not null,
  birth_date date not null
);
*/

import seedbed._

// create a seedbed instance
val s = new Seedbed with TestDBConfiguration

// define the default value for each column
s.define("beatles", Map(
  "first_name" -> "John",
  "middle_name" -> "Winston",
  "last_name" -> "Lennon",
  "birth_date" -> new SimpleDateFormat("yyyy-MM-dd").parse("1940-10-09")
))

// insert a record to database with default value
s.create("beatles")

// insert a record to database overriding default value
s.create("beatles", Map("first_name" -> "Paul"))

// get a record from database
s.get("beatles", Map("first_name" -> "Paul")) //=>Some(Map(first_name -> Paul, ...))
s.get("beatles", Map("first_name" -> "John")) //=>Some(Map(first_name -> John, ...))

// get records from database
s.list("beatles")
```


## License

[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)
