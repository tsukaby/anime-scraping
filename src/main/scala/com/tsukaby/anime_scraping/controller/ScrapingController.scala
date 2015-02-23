package com.tsukaby.anime_scraping.controller

import java.io.File

import com.github.tototoshi.csv.CSVWriter
import com.tsukaby.anime_scraping.service.ScrapingService
import com.tsukaby.anime_scraping.service.Season.{Autumn, Summer, Spring, Winter}

/**
 * Sample
 */
class ScrapingController extends BaseController {

  val scrapingService: ScrapingService = ScrapingService

  override protected def runMain(args: Array[String]): Unit = {
    // リンクを取得
    val year = 2015
    val animesPageUrl = s"http://ja.wikipedia.org/wiki/Category:${year}%E5%B9%B4%E3%81%AE%E3%83%86%E3%83%AC%E3%83%93%E3%82%A2%E3%83%8B%E3%83%A1"
    val links = scrapingService.getWikiLinks(animesPageUrl)

    val f = new File("out.csv")
    val writer = CSVWriter.open(f)
    writer.writeRow("title" :: "url" :: "thumbnailDelay" :: "season" :: "year" :: Nil)

    links foreach { x =>
      Thread.sleep(5000)
      val anime = scrapingService.getAnime(x._2)
      println(anime)

      val season = anime.season.map(_.toString.toLowerCase) getOrElse ""

      writer.writeRow(anime.name :: anime.officialSiteUrl.getOrElse("") :: "0" :: season :: anime.startDateTime.map(_.getYear).getOrElse("") :: Nil)
    }

    writer.close()
  }
}
