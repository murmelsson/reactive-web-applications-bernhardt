package controllers

import play.api.mvc._

class Import extends Controller {

  import play.api.i18n.Lang

  def importWord(
    sourceLanguage: Lang,
    word: String,
    targetLanguage: Lang,
    translation: String) = TODO    // a ref to controllers.Default.todo Action

}
