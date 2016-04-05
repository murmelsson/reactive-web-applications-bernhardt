package actors

import akka.actor._
import akka.persistence
import akka.persistence.{PersistentActor, RecoveryCompleted, RecoveryFailure}

import scala.concurrent.duration._

class CQRSCommandHandler extends PersistentActor with ActorLogging {
  // PersistentActor must have a unique persistenceId:
  override def persistenceId: String = "CQRSCommandHandler"

  // Have to define what recovery means:
  override def receiveRecover: Receive = {
    // http://doc.akka.io/docs/akka/2.4.3/scala/persistence.html
    // "If there is a problem with recovering the state of the actor from the journal when the actor is started,
    // onRecoveryFailure is called (logging the error by default), and the actor will be stopped."
    // i.e. RecoveryFailure is deprecated and instead onRecoveryFailure is called automatically. So comment out:
    case RecoveryFailure(cause) =>
      log.error(cause, "Failed to recover!")

    case RecoveryCompleted =>
      log.info("Recovery completed!")

    case evt: Event =>   //Handle events replayed during a recovery
      handleEvent(evt)
  }

  override def receiveCommand: Receive = {
    case RegisterUser(phoneNumber, username) =>
      // Persist user-regn as UserRegistered event, and call handleEvent-method in the callback slot:
      persist(UserRegistered(phoneNumber, username))(handleEvent)

    case command: Command =>
      context.child(command.phoneNumber).map { reference =>
        // Forward msg to existing ClientCommandHandler:
        log.info("Forwarding command-msg {} to {}", command, reference)
        reference forward command
      } getOrElse {
        // If phoneNumber(=id of ClientCommandHandler) is unknown, there is no such handler-Actor:
        sender() ! "User unknown"
      }
  }

  def handleEvent(event: Event): Unit =
    event match {
      case registered @ UserRegistered(phoneNumber, userName, _) =>
        // Create child-actor ClientCommandHandler:
        context.actorOf(props = Props(classOf[ClientCommandHandler], phoneNumber, userName),
                        name = phoneNumber)
        if (recoveryFinished) {
          // If not in recovery, inform client that registration succeeded:
          sender() ! registered
          // To get the CQRSEventHandler to persist registration to postgres, we need to publish that event:
          context.system.eventStream.publish(registered)
        }
    }
}
