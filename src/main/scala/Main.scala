import zio.blocks.schema.*
import zio.blocks.schema.json.JsonFormat

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.numeric.*

import SchemaIron.given

case class Person(name: String, age: Int :| Positive)

object Person:
  given Schema[Person] = Schema.derived

val jsonCodec = Schema[Person].derive(JsonFormat)


@main def main =

  val person = Person("Esteban", 10.refineUnsafe[Positive])
  val json = jsonCodec.encode(person)

  println(s"JSON: ${new String(json, "UTF-8")}")
