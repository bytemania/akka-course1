package supervision

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}

object Ares {
  def props(athena: ActorRef) = Props[Ares](new Ares(athena))
}

class Ares(athena: ActorRef) extends Actor {

  override def preStart(): Unit = {
    context.watch(athena)
  }


  override def postStop(): Unit = {
    println("Ares postStop...")
  }

  override def receive = {
    case Terminated =>
      context.stop(self)
  }
}

class Athena extends Actor {
  def receive = {
    case msg =>
      println(s"Athena received ${msg}")
      context.stop(self)
  }
}

object Monitoring extends App {
  //create the 'monitoring' actor system
  val system = ActorSystem("Monitoring")

  val athena = system.actorOf(Props[Athena], "athena")

  val ares = system.actorOf(Ares.props(athena), "ares")

  athena ! "hi"

  system.terminate()

}