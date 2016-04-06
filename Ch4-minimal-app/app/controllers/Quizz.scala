package controllers

import javax.inject.Inject

import play.api.mvc._
import play.api.Play.current
import play.api.i18n.Lang
import services.VocabularyService
import actors.QuizzActor

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
    //if (isCorrect) Ok else NotAcceptable
    val correctScore = request.session.get("correct").map(_.toInt).getOrElse(0)
    val wrongScore = request.session.get("wrong").map(_.toInt).getOrElse(0)
    if (isCorrect) {
      Ok.withSession("correct" -> (correctScore + 1).toString,
        "wrong" -> wrongScore.toString)
    } else {
      NotAcceptable.withSession("correct" -> correctScore.toString,
        "wrong" -> (wrongScore + 1).toString)
    }
  }

  def quizzEndpoint(sourceLang: Lang, targetLang: Lang) = {
    WebSocket.acceptWithActor[String, String] { request =>
      out =>
        QuizzActor.props(sourceLang, targetLang, out, vocabulary)
    }
  }

}

