The __@json__ scala macro annotation is the quickest way to add a JSON format to your [Play](http://www.playframework.com/) project's case classes.

#How it works
Just add ```@json``` in front of your case class definition:

```scala
import com.kifi.macros.json

@json case class Person(name: String, age: Int)
```

You can now serialize/deserialize your objects using Play's convenience methods:

```scala
import play.api.libs.json._
val person = Person("Victor Hugo", 46)
val json = Json.toJson(person)
Json.fromJson[Person](json)
```

If the case class contains 2 fields or more, Play's [JSON macro inception](http://www.playframework.com/documentation/2.1.1/ScalaJsonInception) is used. If the case class has only one field (i.e. the class is just a wrapper around another type), then the JSON format is the format of the field itself:

```scala
> @json case class City(name: String)
> val city = City("San Francisco")
> Json.toJson(city)
"San Francisco"
```

This is often more convenient than Play's default format ```{"name": "San Francisco"}```.

If you would rather stick to Play's default format even for single field case classes, you can use ```@jsonstrict``` instead of ```@json```.

#Installation

If you're using Play (version 2.1 or higher) with SBT, you should add the following settings to your build:

```scala

libraryDependencies += "com.kifi" %% "json-annotation" % "0.1"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)
```

If you're not using Play, you will also need to add ```play-json``` to your dependencies:

```scala

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.2.1"
```

This library was tested with both Scala 2.10 and 2.11.
