package services

import biz.enef.angulate.core.HttpPromise
import dashboard._
import org.scalajs.dom._
import utest._
import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSExportAll
/**
// Extend TestSuite so the object can be discovered by test-runner
object GraphDataServiceSuite extends TestSuite {
  val tests = TestSuite {
    //Define a test:
    "GraphDataService should initialise a WebSocket connection" - {
      val growlMock = new GrowlServiceMock
      val mockedWebsocketDataStream = new WebsocketDataStreamMock()

      // Mock the constructor of the WebsocketService using a native JavaScript function:
      val mockedWebsocketService: js.Function = {
        (url: String, options: js.UndefOr[js.Dynamic]) =>
          mockedWebsocketDataStream.asInstanceOf[WebsocketDataStream]
      }

      // Initialise the real GraphDataService using the mocks:
      new GraphDataService(
        mockedWebsocketService.asInstanceOf[WebsocketService],
        growlMock.asInstanceOf[GrowlService]
      )

      // Make sure the WebsocketDataStream has actually been initialised:
      assert(mockedWebsocketDataStream.isInitialized)
    }//should-test
  }//test-suite
}


// Export all public members of the mocked class to JavaScript:
//@JSExportAll
class GrowlServiceMock

//@JSExportAll
class WebsocketDataStreamMock {
  val isInitialized = true
  def send[T](data: js.Any): HttpPromise[T] = ???
  def onMessage(
    callback: js.Function1[MessageEvent, Unit],
    options: UndefOr[js.Dynamic] = js.undefined
  ): Unit = ()
  def onClose(callback: js.Function1[CloseEvent, Unit]): Unit = {}
  def onOpen(callback: js.Function1[js.Dynamic, Unit]): Unit = {}
}

*/

/**
  * (Section 8.4.1 end of) "Since there is no mocking library that runs with Scala.js yet we need to mock the
dependencies on our service by hand. Mocking façade traits isn’t a straight-forward
process and requires a bit of preparations - we can’t simply extend the existing façade
traits and override the implementation because of the way Scala.js compilation is
designed. Instead, we need to create the mock classes as stand-alone Scala classes that we
are exporting to Javascript using the JSExportAll annotation and casting them back
to the façade trait.
Another specific point to our example is that the WebsocketService itself only
defines a constructor (the apply method), so in order to mock it we need to implement
this constructor mock as a native Javascript function and have it return our mock of the
WebsocketDataStream which contains all of the interesting functions.
This manual approach to testing is certainly not as convenient as the tooling provided
on the JVM for the moment, but chances are that there will be a mocking library
available for Scala.js projects soon."
  */
