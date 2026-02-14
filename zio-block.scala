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

def fetchLinks(title: String): Seq[WikiResult] = {
  Jsoup
    .connect(s"https://en.wikipedia.org/wiki/$title")
    .header("User-Agent", "My Scraper")
    .get()
    .select("main p a")
    .asScala
    .toSeq
    .map(a => (a.attr("href"), a.attr("href")))
    .collect { case (s"/wiki/$rest", url) => WikiResult(rest, s"https://en.wikipedia.org$url") }
}

def scrapper(startArticle: String, depth: Int): Set[WikiResult] = {
  val start = WikiResult(startArticle, s"https://en.wikipedia.org/wiki/$startArticle")
  (0 until depth)
    .foldLeft((Set(start), Set(start))) {
      case ((seen, current), _) =>
        val next = current.flatMap(r => fetchLinks(r.title)) diff seen
        (seen ++ next, next)
    }
    ._1
}

case class WikiResult(title: String, url: String)

object WikiResult:
   given Schema[WikiResult] = Schema.derived

val wikiCodec = Schema[WikiResult].derive(JsonFormat)

@main def main(startArticle: String, depth: Int) =
  val answers = scrapper(startArticle, depth)
   answers.foreach { result =>
     val encoded: Array[Byte] = wikiCodec.encode(result)
     println(wikiCodec.encode(result))
     val decoded: Either[SchemaError, WikiResult] = wikiCodec.decode(encoded)

     println(wikiCodec.decode(encoded))

  }


// scala-cli run zio-block.scala -- colombia 1
