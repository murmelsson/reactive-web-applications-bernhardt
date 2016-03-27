package actors

import akka.actor.{ Actor, ActorRef, Props }
import play.api.i18n.Lang
import services.VocabularyService

class QuizzActor(out: ActorRef,
  sourceLang: Lang,
  targetLang: Lang,
  vocabulary: VocabularyService)
    extends Actor {

  private var word = ""

  override def preStart(): Unit = sendWord()

  def receive = {
    case translation: String if vocabulary.verify(sourceLang, word, targetLang, translation) =>
      out ! "Correct answer!"
      sendWord()
    case _ =>
      out ! "Wrong, try again!"
  }

  def sendWord() = {
    vocabulary.findRandomVocabulary(sourceLang, targetLang).map { v =>
      out ! s"----\nPlease translate '${v.word}'"
      word = v.word
    } getOrElse {
      out ! s"No translation-pair currently known by this app for '${sourceLang.code}'" +
        " and '${targetLang.code}'"
    }
  }
}

object QuizzActor {
  def props(sourceLang: Lang,
    targetLang: Lang,
    out: ActorRef,
    vocabulary: VocabularyService): Props =
    Props(classOf[QuizzActor], out, sourceLang, targetLang, vocabulary)

}

