package com.example

import zio.blocks.schema.*
import zio.blocks.schema.json.JsonFormat


import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.numeric.*

case class Person(name: String, age: Int :| Positive)

object Person:
  given Schema[Person] = Schema.derived

val jsonCodec = Schema[Person].derive(JsonFormat)


@main def main =

  val Test = Person("Esteban", 1)

  println(s"Saved results in 5 formats")
