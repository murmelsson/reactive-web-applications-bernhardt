
import org.scalatest._
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._

class ApplicationSpec
extends PlaySpec
with OneServerPerSuite
with OneBrowserPerSuite
with FirefoxFactory {

  "The Application" must {
    "display a text when clicking on a button" in {

      go to (s"http://localhost:$port")
      pageTitle mustBe "Moikka!! Hello!!"
      click on find(id("button")).value

      eventually {
        val expectedText = app.configuration.getString("text")
        find(id("text")).map(_.text) mustBe expectedText
      }

    }
  }

}
