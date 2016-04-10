package dashboard

import biz.enef.angulate._
import org.scalajs.dom._
import scalajs.js.Dynamic
import scala.scalajs.js

class DashboardCtrl($scope: Dynamic, graphDataService: GraphDataService)
extends ScopeController {
  
  $scope.hello = "Hello world!!"

  $scope.helloBack = () => console.log("Well hello therr!!")

  graphDataService.fetchGraph(GraphType.MonthlySubscriptions, { (graphData: js.Dynamic) =>
    console.log(graphData)
    $scope.monthlySubscriptions = graphData
  })

}
