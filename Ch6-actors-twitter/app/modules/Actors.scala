package modules

import javax.inject._
import actors.StatisticsProvider
import akka.actor.{ActorSystem, Props}
import com.google.inject.AbstractModule

class Actors @Inject()(system: ActorSystem)
extends ApplicationActors {

  system.actorOf(
    //props = StatisticsProvider.props,
    Props[StatisticsProvider],
    name = "statisticsProvider"
  )
}

trait ApplicationActors

class ActorsModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ApplicationActors])
      .to(classOf[Actors]).asEagerSingleton
  }
}
