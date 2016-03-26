package controllers

import javax.inject.Inject

import play.api.mvc._
import play.api.i18n.Lang
import services.VocabularyService

class Quizz @Inject() (vocabulary: VocabularyService) extends Controller {
  def quizz(sourceLanguage: Lang, targetLanguage: Lang) = Action {
    vocabulary.findRandomVocabulary(sourceLanguage, targetLanguage).map { v =>
      Ok(v.word)
    } getOrElse {
      NotFound
    }
  }

  def check(sourceLanguage: Lang, word: String, targetLanguage: Lang, translation: String) = Action { request =>
    val isCorrect: Boolean = vocabulary.verify(sourceLanguage, word, targetLanguage, translation)
    if (isCorrect) Ok else NotAcceptable
  }
}
