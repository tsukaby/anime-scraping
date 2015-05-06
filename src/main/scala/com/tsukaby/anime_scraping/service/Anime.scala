package com.tsukaby.anime_scraping.service

import org.joda.time.LocalDate

case class Anime(
  name: String,
  startDateTime: Option[LocalDate],
  season: Option[Season],
  productionCompanyId: Int,
  directorId: Option[Int],
  originalPiece: String,
  officialSiteUrl: String,
  wikipediaSiteUrl: String
)
