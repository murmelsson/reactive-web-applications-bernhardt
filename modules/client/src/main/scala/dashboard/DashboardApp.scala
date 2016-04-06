package dashboard

import scala.scalajs.js.JSApp
//import org.scalajs.dom._
import biz.enef.angulate.ext.{Route, RouteProvider}
import biz.enef.angulate._

object DashboardApp extends JSApp {
  def main(): Unit = {
    //document.getElementById("scalajs").innerHTML =
    //  "Hello from Scala.js!!"

    val module = angular.createModule("dashboard", Seq("ngRoute", "ngWebSocket"))

    module.controllerOf[DashboardCtrl]
    module.config { ($routeProvider: RouteProvider) =>
      $routeProvider
        .when("/dashboard", Route(
          templateUrl = "/assets/partials/dashboard.html",
          controller = "dashboard.DashboardCtrl")
        ).otherwise(Route(redirectTo = "/dashboard"))
    }
  }
}
