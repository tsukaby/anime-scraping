package com.tsukaby.anime_scraping.controller

/**
 * バッチ処理の基底クラス
 */
trait BaseController {

  /**
   * 初期化処理
   */
  protected def before(): Unit = {

  }

  /**
   * 起動用
   *
   * @param args プログラム実行時に渡された引数から先頭要素を除いたもの
   */
  def run(args: Array[String]): Unit = {

    before()

    runMain(args)

    after()
  }

  /**
   * 処理本体
   *
   * @param args プログラム実行時に渡された引数から先頭要素を除いたもの
   */
  protected def runMain(args: Array[String]): Unit

  /**
   * 後処理
   */
  protected def after(): Unit = {

  }


}
