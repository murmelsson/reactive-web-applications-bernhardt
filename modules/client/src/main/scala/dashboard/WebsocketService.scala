package dashboard

import biz.enef.angulate.core.{HttpPromise, ProvidedService}
import org.scalajs.dom._
import scala.scalajs.js
import scala.scalajs.js.UndefOr
//import js.annotation._   //in case we would use @ScalaJSDefined

// Extend scalajs-angulate's ProvidedService helper trait,
// so that this service is marked as provided automatically by AngularJS
@js.native
trait WebsocketService extends ProvidedService {
  // apply-method mimics JavaScript constructor of the angular-websocket:
  def apply(
    url: String,
    options: UndefOr[Dynamic] = js.undefined
  ): WebsocketDataStream = js.native
}


// Define a facade for the object returned by service-constructor
@js.native
trait WebsocketDataStream extends js.Object {
  // Define type-safe wrapper for send-method (which returns an HttpPromise):
  def send[T](data: js.Any): HttpPromise[T] = js.native

  def onMessage(
    // Define callback-parameter of onMessage-method as a function 
    // that takes a MessageEvent and returns nothing
    callback: js.Function1[MessageEvent, Unit],
    // Define (optional) options-parameter of onMessage-method
    // as a Dynamic value:
    options: UndefOr[js.Dynamic] = js.undefined
  ): Unit = js.native

  def onClose(callback: js.Function1[CloseEvent, Unit]): Unit = js.native
  def onOpen(callback: js.Function1[js.Dynamic, Unit]): Unit = js.native
}

/** The only specific aspect to AngularJS in this façade is the use of the ProvidedService trait which eases service discovery - in order to wrap any Javascript library it is sufficient to extend js.Object . */
