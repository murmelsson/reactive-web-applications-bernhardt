package dashboard

import biz.enef.angulate.core.ProvidedService

import scala.scalajs.js

@js.native     //i.e. scala.scalajs.js.native
trait GrowlService extends ProvidedService {
  def info(message: String): Unit = js.native
  def warning(message: String): Unit = js.native
  def success(message: String): Unit = js.native
  def error(message: String): Unit = js.native
}
