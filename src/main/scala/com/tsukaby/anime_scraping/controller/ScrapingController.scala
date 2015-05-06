package com.tsukaby.anime_scraping.controller

import java.io.File

import com.github.tototoshi.csv.CSVWriter
import com.tsukaby.anime_scraping.service.ScrapingService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
 * Sample
 */
class ScrapingController extends BaseController {

  val scrapingService: ScrapingService = ScrapingService

  override protected def runMain(args: Array[String]): Unit = {
    require(args.length == 1)

    // リンクを取得
    val year = args(0).toInt
    val animesPageUrl = s"http://ja.wikipedia.org/wiki/Category:${year}%E5%B9%B4%E3%81%AE%E3%83%86%E3%83%AC%E3%83%93%E3%82%A2%E3%83%8B%E3%83%A1"
    val links = scrapingService.getWikiLinks(animesPageUrl)

    val f = new File(s"animes_$year.csv")
    val writer = CSVWriter.open(f)
    writer.writeRow("title" :: "url" :: "thumbnailDelay" :: "season" :: "year" :: Nil)

    val process = for {
      links <- links
    } yield {
      links foreach { x =>
        for {
          animeOpt <- scrapingService.getAnime(x._2)
        } yield for {
          anime <- animeOpt
        } {
          println(anime)
          val season = anime.season.map(_.toString.replaceAll("$", "").toLowerCase) getOrElse ""
          writer.writeRow(anime.name :: anime.officialSiteUrl.getOrElse("") :: "0" :: season :: anime.startDateTime.map(_.getYear).getOrElse("") :: Nil)
        }

      }
    }

    Await.ready(process, Duration.Inf)

    writer.close()
  }
}
