package com.tsukaby.anime_scraping.service

import org.joda.time.LocalDate

case class Anime(
                  name: String,
                  startDateTime: Option[LocalDate],
                  season: Option[Season],
                  officialSiteUrl: Option[String]
                  )
