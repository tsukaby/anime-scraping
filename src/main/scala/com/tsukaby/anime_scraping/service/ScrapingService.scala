package com.tsukaby.anime_scraping.service

import java.io.{File, PrintWriter, FileNotFoundException, StringReader}
import java.net.URLDecoder

import com.github.nscala_time.time.Imports._
import com.tsukaby.anime_scraping.service.Season.{Autumn, Spring, Summer, Winter}
import dispatch.Defaults._
import dispatch._
import nu.validator.htmlparser.common.XmlViolationPolicy
import nu.validator.htmlparser.sax.HtmlParser
import org.xml.sax.InputSource

import scala.collection.immutable
import scala.concurrent.Future
import scala.io.Source
import scala.language.postfixOps
import scala.xml.parsing.NoBindingFactoryAdapter
import scala.xml.{Node, Text}

/**
 * web pageをscrapingします
 */
trait ScrapingService extends BaseService {

  def getAnime(link: String): Future[Option[Anime]] = {
    require(link.nonEmpty)

    val nodeF = getNode(link)
    val linkF = getOfficialSiteLink(link)

    for {
      nodeOpt <- nodeF
      url <- linkF
    } yield for {
      node <- nodeOpt
    } yield {
      // title
      val title: String = node \\ "h1" find (_ \ "@id" contains Text("firstHeading")) map (_.text) getOrElse ("")

      // year
      val entryBody = node \\ "table" filter (_ \ "@class" contains Text("infobox bordered"))

      val year: immutable.Seq[Array[String]] = for {
        b <- entryBody \\ "tr" if b.text contains "放送期間"
      } yield {
          b.text.split("\n")
        }

      val strDateTime = if (year.length > 0 && year(0).length > 2) {
        val monthCharIndex = year(0)(2).indexOf("月")
        Some(year(0)(2).take(monthCharIndex + 1))
      } else {
        None
      }


      val start = try {
        strDateTime.map(x => DateTimeFormat.forPattern("yyyy年M月").parseDateTime(x).toLocalDate)
      } catch {
        case _: Throwable =>
          None
      }

      val season = start.map(toSeason)

      Anime(title, start, season, url)

    }

  }

  private def getOfficialSiteLink(link: String): Future[Option[String]] = {
    require(link.nonEmpty)

    for {
      nodeOpt <- getNode(link)
    } yield for {
      node <- nodeOpt
    } yield {
      //公式サイトURLの候補
      val links = node \\ "ul" \\ "li" \\ "a" filter (_ \ "@class" contains Text("external text"))
      val candidate: Seq[(String, String)] = links map (x => (x.text, x.attribute("href").map(_(0).text).getOrElse("")))

      //各URLに公式サイトである可能のポイント付けを行う
      val sorted = candidate.map(x => calcPoint(x) -> x._2).sortBy(_._1).reverse

      // 一番先頭の要素がアニメ公式サイト（である可能性が高い）
      sorted.map(_._2).head
    }
  }

  private def calcPoint(obj: (String, String)) = {
    var point = 0

    // 以下の要素を持つリンクがアニメ公式サイトである可能性が高い
    if (obj._1.contains("公式")) {
      point += 2
    }
    if (obj._1.contains("オフィシャル")) {
      point += 2
    }

    if (obj._1.contains("アニメ")) {
      point += 1
    }
    if (obj._1.contains("TV")) {
      point += 1
    }
    if (obj._1.contains("テレビ")) {
      point += 1
    }
    if (obj._1.contains("TVアニメ")) {
      point += 2
    }
    if (obj._2.contains(".tv")) {
      point += 1
    }

    // exclusion
    if (obj._2.contains("twitter.com")) {
      point -= 100
    }
    if (obj._1.contains("ラジオ")) {
      point -= 3
    }
    if (obj._1.contains("アニメイト")) {
      point -= 3
    }


    point
  }

  private def toSeason(start: LocalDate) = {
    start.getMonthOfYear match {
      case 1 | 2 | 3 => Winter
      case 4 | 5 | 6 => Spring
      case 7 | 8 | 9 => Summer
      case 10 | 11 | 12 => Autumn
    }
  }

  /**
   * 引数で指定した年のアニメの一覧を取得します。
   * @param pageUrl 対象とするアニメ一覧のWikiページ
   * @return アニメのタイトルとアニメのWikiページのリンク
   */
  def getWikiLinks(pageUrl: String): Future[Seq[(String, String)]] = {
    for {
      nodeOpt <- getNode(pageUrl)
    } yield {
      nodeOpt match {
        case Some(node) =>
          val linksBody = node \\ "div" filter (_ \ "@id" contains Text("mw-pages"))

          (linksBody \\ "a") map { x =>
            val title = x.text
            val href = "http://ja.wikipedia.org" + x.attribute("href").map(_(0).text).getOrElse("")
            (title, href)
          }
        case None =>
          Seq()
      }
    }
  }

  private def getNode(pageUrl: String): Future[Option[Node]] = {
    require(pageUrl.nonEmpty)

    val filePath = cachedFilePath(pageUrl)


    val file = try {
      val body = Source.fromFile(filePath).getLines().foldLeft("")(_ + _)
      println(s"Use cache : $filePath")
      Future(Some(body))
    } catch {
      case e: FileNotFoundException =>
        println(s"No cached : $filePath")
        var req = url(pageUrl)
        req = req <:< immutable.Map("User-Agent" -> "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0)")

        Http(req).either.map {
          case Right(content) =>
            val body = content.getResponseBody("UTF-8")

            // caching
            val writer = new PrintWriter(new File(filePath))
            writer.write(body)
            writer.close()

            Some(body)
          case Left(StatusCode(404)) =>
            println(s"Not found : $pageUrl")
            None
          case Left(err) =>
            println(s"Something error : ${err.getMessage}")
            None
        }
    }

    file.map(_.map(toNode))
  }

  private def cachedFilePath(pageUrl: String): String = {
    require(pageUrl.nonEmpty)
    val filePath = ".tmp/" + pageUrl.replace("http://", "").replace("/", "_") + ".html"

    URLDecoder.decode(filePath, "utf-8")
  }

  def toNode(str: String): Node = {
    val hp = new HtmlParser
    hp.setNamePolicy(XmlViolationPolicy.ALLOW)
    hp.setCommentPolicy(XmlViolationPolicy.ALLOW)
    val saxer = new NoBindingFactoryAdapter
    hp.setContentHandler(saxer)
    hp.parse(new InputSource(new StringReader(str)))
    saxer.rootElem
  }

}

object ScrapingService extends ScrapingService
