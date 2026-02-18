import zio.blocks.schema.*
import zio.blocks.schema.json.JsonFormat
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.numeric.*

case class Person(name: String, age: Int :| Positive)

object Person:
  // Debug version
  given Schema[Int :| Positive] = 
    Schema[Int].transformOrFail(
      i => {
        println(s"Refining: $i")
        val result = i.refineEither[Positive].left.map(SchemaError.validationFailed)
        println(s"Refine result: $result")
        result
      },
      refined => {
        val base = refined.asInstanceOf[Int]
        println(s"Converting back: refined=$refined, base=$base")
        base
      }
    )
  
  given Schema[Person] = Schema.derived

@main def main =

  val jsonCodec = Schema[Person].derive(JsonFormat)
  val person = Person("Esteban", 10.refineUnsafe[Positive])
  
  println(s"Person: $person")
  println(s"Age value: ${person.age}")
  
  val json = jsonCodec.encode(person)

  println(s"JSON: ${new String(json, "UTF-8")}")
  
  // Test validation
  val validJson = """{"name":"Alice","age":25}"""
  val invalidJson = """{"name":"Bob","age":-5}"""
  
  println(s"Valid decode: ${jsonCodec.decode(validJson.getBytes)}")
  println(s"Invalid decode: ${jsonCodec.decode(invalidJson.getBytes)}")

