//> using dep "dev.zio::zio-blocks-schema:0.017"
//> using dep "dev.zio::zio-blocks-schema-avro:0.017"
//> using dep "dev.zio::zio-blocks-schema-toon:0.017"
//> using dep "dev.zio::zio-blocks-schema-messagepack:0.017"
//> using dep "dev.zio::zio-blocks-schema-thrift:0.017"
//> using dep "dev.zio::zio-blocks-schema-bson:0.017"

//> using dep "org.jsoup:jsoup:1.22.1"

// https://mill-build.org/mill/scalalib/script.html#_script_use_cases

import zio.blocks.schema.*
import zio.blocks.schema.json.JsonFormat
import zio.blocks.schema.avro.AvroFormat
import zio.blocks.schema.toon.ToonFormat
import zio.blocks.schema.msgpack.MessagePackFormat
import zio.blocks.schema.thrift.ThriftFormat

import scala.jdk.CollectionConverters.*
import org.jsoup.Jsoup

case class Person(name: String, age: Int)

object Person:
  given Schema[Person] = Schema.derived

val jsonCodec = Schema[Person].derive(JsonFormat)

def fetchLinks(title: String): Seq[WikiResult] = {
  Jsoup
    .connect(s"https://en.wikipedia.org/wiki/$title")
    .header("User-Agent", "My Scraper")
    .get()
    .select("main p a")
    .asScala
    .toSeq
    .map(a => (a.attr("href"), a.attr("href")))
    .collect { case (s"/wiki/$rest", url) =>
      WikiResult(rest, s"https://en.wikipedia.org$url")
    }
}

def scrapper(startArticle: String, depth: Int): Set[WikiResult] = {
  val start =
    WikiResult(startArticle, s"https://en.wikipedia.org/wiki/$startArticle")
  (0 until depth)
    .foldLeft((Set(start), Set(start))) { case ((seen, current), _) =>
      val next = current.flatMap(r => fetchLinks(r.title)) diff seen
      (seen ++ next, next)
    }
    ._1
}

case class WikiResult(title: String, url: String)

object WikiResult:
  given Schema[WikiResult] = Schema.derived

val wikiJsonCodec = Schema[WikiResult].derive(JsonFormat)
val wikiAvroCodec = Schema[Person].derive(AvroFormat) // Avro
val wikiToonCodec = Schema[Person].derive(ToonFormat) // TOON (LLM-optimized)
val wikiMsgpackCodec = Schema[Person].derive(MessagePackFormat) // MessagePack
val wikiThriftCodec = Schema[Person].derive(ThriftFormat) // Thrift

@main def main(startArticle: String, depth: Int) =
  val answers: Set[WikiResult] = scrapper(startArticle, depth)
  answers.foreach { result =>
    val encoded: Array[Byte] = wikiJsonCodec.encode(result)
    val jsonString: String = new String(encoded)
    println(jsonString)

    val decoded: Either[SchemaError, WikiResult] = wikiJsonCodec.decode(encoded)
    decoded match {
      case Right(wiki) => println(s"Decoded: ${wiki.title}")
      case Left(error) => println(s"Error: $error")
    }
  }

// scala-cli run zio-block.scala -- colombia 1
