//> using repository "ivy:file:///Users/esteban-ziverge/.ivy2/local/[organisation]/[module]/[revision]/[type]s/[artifact](-[classifier]).[ext]"

//> using dep "dev.zio::zio-blocks-schema:0.017"
//> using dep "dev.zio::zio-blocks-schema-avro:0.017"
//> using dep "dev.zio::zio-blocks-schema-toon:0.017"
//> using dep "dev.zio::zio-blocks-schema-messagepack:0.017"
//> using dep "dev.zio::zio-blocks-schema-thrift:0.017"
//> using dep "dev.zio::zio-blocks-schema-bson:0.017"

//> using dep "org.jsoup:jsoup:1.22.1"

//> using dep "io.github.iltotore::iron::3.2.3"


// https://mill-build.org/mill/scalalib/script.html#_script_use_cases

import zio.blocks.schema.*
import zio.blocks.schema.json.JsonFormat
import zio.blocks.schema.avro.AvroFormat
import zio.blocks.schema.toon.ToonFormat
import zio.blocks.schema.msgpack.MessagePackFormat
import zio.blocks.schema.thrift.ThriftFormat

import scala.jdk.CollectionConverters.*
import org.jsoup.Jsoup

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.numeric.*

def log(x: Double :| Positive): Double =
  Math.log(x)

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
val wikiAvroCodec = Schema[WikiResult].derive(AvroFormat)
val wikiToonCodec = Schema[WikiResult].derive(ToonFormat)
val wikiMsgpackCodec = Schema[WikiResult].derive(MessagePackFormat)
val wikiThriftCodec = Schema[WikiResult].derive(ThriftFormat)

@main def main(startArticle: String, depth: Int) =
  val answers = scrapper(startArticle, depth).toList
  
  val jsonList = Schema[List[WikiResult]].derive(JsonFormat)
  val avroList = Schema[List[WikiResult]].derive(AvroFormat)
  val toonList = Schema[List[WikiResult]].derive(ToonFormat)
  val msgpackList = Schema[List[WikiResult]].derive(MessagePackFormat)
  val thriftList = Schema[List[WikiResult]].derive(ThriftFormat)

  java.nio.file.Files.writeString(
    java.nio.file.Paths.get("json.txt"),
    new String(jsonList.encode(answers))
  )
  java.nio.file.Files.writeString(
    java.nio.file.Paths.get("avro.txt"),
    new String(avroList.encode(answers))
  )
  java.nio.file.Files.writeString(
    java.nio.file.Paths.get("toon.txt"),
    new String(toonList.encode(answers))
  )
  java.nio.file.Files.writeString(
    java.nio.file.Paths.get("msgpack.txt"),
    new String(msgpackList.encode(answers))
  )
  java.nio.file.Files.writeString(
    java.nio.file.Paths.get("thrift.txt"),
    new String(thriftList.encode(answers))
  )

  println(s"Saved ${answers.size} results in 5 formats")
  log(-1)
// scala-cli run zio-block.scala -- colombia 1
