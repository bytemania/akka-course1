package supervision

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorRef, ActorSystem, OneForOneStrategy, Props}
import supervision.Aphrodite.{RestartException, ResumeException, StopException}

import scala.concurrent.duration.DurationInt

class Aphrodite extends Actor {


  override def preStart(): Unit = {
    println("Aphrodite preStart hook...")
  }


  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("Aphrodite preRestart hook...")
    super.preRestart(reason, message)
  }

  override def postRestart(reason: Throwable): Unit = {
    println("Aphrodite postRestart hook...")
    super.postRestart(reason)
  }


  override def postStop(): Unit = {
    println("Aphrodite postStop...")
  }

  override def receive = {
    case "Resume" => throw ResumeException
    case "Stop" => StopException
    case "Restart" => throw RestartException
    case _ => throw new Exception
  }
}

object Aphrodite {
  case object ResumeException extends Exception
  case object StopException extends Exception
  case object RestartException extends Exception
}

object Hera {
  def props(childRef: ActorRef) = Props[Hera](new Hera(childRef))
}

class Hera(var childRef: ActorRef) extends Actor {

  override def supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 second) {
    case ResumeException => Resume
    case RestartException => Restart
    case StopException => Stop
    case _: Exception => Escalate
  }

  override def receive = {
    case msg =>
      println(s"Hera received $msg")
      childRef ! msg
      Thread.sleep(100)
  }
}

object Supervision extends App {
  //Create the 'supervision' actor system
  val system = ActorSystem("Supervision")

  val aphrodite = system.actorOf(Props[Aphrodite], "aphrodite")

  val hera = system.actorOf(Hera.props(aphrodite), "hera")

  hera ! "Stop"
  Thread.sleep(1000)
  println()

  system.terminate()
}

