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

def scrapper(startArticle: String, depth: Int): Set[String] = {
  (0 until depth)
    .foldLeft(Set(startArticle), Set(startArticle)) {
      case ((seen, current), _) =>
        val next = current.flatMap(fetchLinks) diff seen
        (seen ++ next, next)
    }
    ._1
}

@main def main(startArticle: String, depth: Int) =
  val answers =  scrapper(startArticle, depth)
  println(s"$answers")



// scala-cli run zio-block.scala -- colombia 1
