package com.kifi.macros

import org.specs2.mutable.Specification
import play.api.libs.json._

@json case class City(name: String)

@json case class Person(name: String, age: Int)

class JsonFormatAnnotationTest extends Specification {

  "@json annotation" should {

    "create correct formatter for case class with 1 field" in {

      val city = City("San Francisco")
      val json = Json.toJson(city)
      json === JsString("San Francisco")
      Json.fromJson[City](json) === JsSuccess(city)
    }

    "create correct formatter for case class with >= 2 fields" in {

      val person = Person("Victor Hugo", 46)
      val json = Json.toJson(person)
      json === Json.obj(
        "name" -> "Victor Hugo",
        "age" -> 46
      )
      Json.fromJson[Person](json) === JsSuccess(person)
    }
  }
}
