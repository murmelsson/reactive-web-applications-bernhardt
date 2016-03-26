package controllers

import play.api.mvc._
import javax.inject.Inject

import models.Vocabulary
import services.VocabularyService

class Import @Inject() (vocabulary: VocabularyService)
    extends Controller {

  import play.api.i18n.Lang

  def importWord(
    sourceLanguage: Lang,
    word: String,
    targetLanguage: Lang,
    translation: String) =
    Action { request =>
      val added = vocabulary.addVocabulary(
        Vocabulary(sourceLanguage, targetLanguage, word, translation))
      if (added) Ok
      else Conflict
    }

}
