package com.tsukaby.anime_scraping

import com.tsukaby.anime_scraping.controller.BaseController

/**
 * バッチアプリケーションを起動するためのクラス・エントリポイントです。
 */
object Main {

  /**
   * エントリポイント
   * @param args 第一引数：実行するバッチコントローラクラス名　第二引数以降：コントローラに渡す引数
   */
  def main(args: Array[String]) {
    require(args.length > 0, "引数が足りません。引数にコントローラ名を指定してください。")

    // リフレクションによって実行するコントローラを取得
    val targetControllerName = args.head
    val clazz =  try {
      Class.forName("com.tsukaby.anime_scraping.controller." + targetControllerName)
    } catch {
      case e:ClassNotFoundException =>
        throw new IllegalArgumentException(s"Class not found : $targetControllerName", e)
    }

    // 対象クラス(バッチ処理)のインスタンスを取得
    val controller: BaseController = clazz.newInstance().asInstanceOf[BaseController]

    // 引数を渡して実行
    controller.run(args.tail)

    System.exit(0)
  }

}