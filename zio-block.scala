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

import scala.jdk.CollectionConverters.*
import org.jsoup.Jsoup

case class Person(name: String, age: Int)

object Person:
  given Schema[Person] = Schema.derived

val jsonCodec = Schema[Person].derive(JsonFormat)

def fetchLinks(title: String): Seq[String] = {
  Jsoup
    .connect(s"https://en.wikipedia.org/wiki/$title")
    .header("User-Agent", "My Scraper")
    .get()
    .select("main p a")
    .asScala
    .toSeq
    .map(_.attr("href"))
    .collect { case s"/wiki/$rest" => rest }
}

def scrapper(startArticle: String, depth: Int) = {
  var seen = Set(startArticle)
  var current = Set(startArticle)
  for (i <- Range(0, depth)) {
    current = current.flatMap(fetchLinks(_)).filter(!seen.contains(_))
    seen = seen ++ current
  }
  seen
  println(seen)
}


@main def main = ???
// scala-cli run zio-block.scala -- colombia 1
