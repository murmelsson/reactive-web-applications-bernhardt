package dashboard

import biz.enef.angulate._
import org.scalajs.dom._
import scala.scalajs.js.{Dynamic, JSON}
import scala.collection._

class GraphDataService($websocket: WebsocketService) extends Service {
//class GraphDataService($websocket: WebsocketService, growl: GrowlService) extends Service {
  println("In angularService 'GraphDataService', about to connect to websocket-endpoint...")
  // Connect to Websocket endpoint (defined via route: /graphs to Application.graphs-method,
  // which then passes its ActorRef to Application.DashboardClient, which gets data from Postgres
  // and messages ActorRef with a Json-obj of pairs:
  //    "graph_type" -> GraphType.MonthlySubscriptions,
  //    "labels" -> Json.toJson(monthlyCounts.keys),
  //    "series" -> Json.arr("Subscriptions"),
  //    "data" -> Json.arr(Json.toJson(monthlyCounts.values))
  val dataStream = $websocket("ws://localhost:9000/graphs", Dynamic.literal("reconnectIfNotNormalClose" -> true))

  private val callbacks =
    mutable.Map.empty[GraphType.Value, Dynamic => Unit]

  // The ngController (DashboardCtrl) calls this method supplying GraphType.MonthlySubscriptions and
  // a callback(i.e. a do-later-when-data-is-ready-function) - e.g. console.log(returned-graph-data)
  // However the fetchGraph method itself obviously doesn't need to know the callback-details, just what the
  // callback "listener" is. fetchGraph itself doesn't directly call back with the json-data, but via
  // passing the task on to dataStream.onMessage
  // Fetching monthly subscriptions by remembering the callback and sending a message to datastream passing down
  // our GraphType.toString
  def fetchGraph(graphType: GraphType.Value, callback: Dynamic => Unit) = {
    callbacks += graphType -> callback
    println("In GDS fetchGraph")
    dataStream.send(graphType.toString)  // dashboard.WebsocketDataStream
                                         // def send[T](data: Any): HttpPromise[T]
  }

  // dashboard.WebsocketDataStream
  // def onMessage(callback: Function1[dom.MessageEvent, Unit],
  //              options: UndefOr[Dynamic] = js.undefined): Unit
  // Note that here we effectively execute the callback fn passed in from the ngController e.g.
  //   (graphData: js.Dynamic) =>
  //     console.log(graphData)
  //     $scope.monthlySubscriptions = graphData
  dataStream.onMessage { (event: MessageEvent) =>
    console.log("In GDS dataStream.onMessage with Event" + event)
    val json: Dynamic = JSON.parse(event.data.toString)
    val graphType = GraphType.withName(json.graph_type.toString)  //scala.Enumeration
                                                                  //final def withName(s: String): Enumeration.this.Value
                                                                  // Return a Value from this Enumeration whose name matches the argument s.
                                                                  // The names are determined automatically via reflection.
                                                                  // Parameters: s - an Enumeration name
    callbacks.get(graphType).map { callback =>       // Pattern: callbacks: Map[GraphType.Value, (Dynamic) => Unit]
      // Attempting to find the appropriate callback for a message and calling back to it with the json-data
      callback(json)   // callback: (Dynamic) => Unit
    } getOrElse {
      console.log(s"Unknown graph type $graphType")
    }
  }

  dataStream.onClose { (event: CloseEvent) =>
    //growl.error(s"Server connection closed, attempting to reconnect")
    println("ws-connection closed")
  }

  dataStream.onOpen { (event: Dynamic) =>
    //growl.info("Server connection established")
    println("ws-connection opened")
  }
}

object GraphType extends Enumeration {
  val MonthlySubscriptions = Value   // Pattern: MonthlySubscriptions: GraphType.Value
}
