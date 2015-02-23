package com.tsukaby.anime_scraping.service

sealed trait Season {
  val typeId: Int

  override def toString: String = super.getClass.getSimpleName.replaceAll("$", "")
}

case object Season {

  implicit def valueOf(typeId: Int): Season = {
    typeId match {
      case Winter.typeId => Winter
      case Spring.typeId => Spring
      case Summer.typeId => Summer
      case Autumn.typeId => Autumn
      case _ => throw new IllegalArgumentException("Bad number.")
    }
  }

  implicit def toInt(season: Season): Int = {
    season.typeId
  }

  object Winter extends Season {
    override val typeId: Int = 1
  }

  object Spring extends Season {
    override val typeId: Int = 2
  }

  object Summer extends Season {
    override val typeId: Int = 3
  }

  object Autumn extends Season {
    override val typeId: Int = 4
  }

}