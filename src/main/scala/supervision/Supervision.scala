package supervision

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import supervision.Checker.{BlackUser, CheckUser, WhiteUser}
import supervision.Recorder.NewUser
import supervision.Storage.AddUser

case class User(username: String, email: String)

object Recorder {
  sealed trait RecorderMsg
  case class NewUser(user: User) extends RecorderMsg

  def props(checker: ActorRef, storage: ActorRef) = Props[Recorder](new Recorder(checker, storage))
}

object Checker {
  sealed trait CheckerMsg
  case class CheckUser(user: User) extends CheckerMsg

  sealed trait CheckerResponse
  case class BlackUser(user: User) extends CheckerResponse
  case class WhiteUser(user: User) extends CheckerResponse
}

object Storage {
  sealed trait StoreMsg
  case class AddUser(user:User) extends StoreMsg
}

class Storage extends Actor {
  var users = List.empty[User]

  override def receive = {
    case AddUser(user) =>
      println(s"Storage: $user added")
      users ::= user
  }
}

class Checker extends Actor {
  val blackList = List (User("Adam", "adam.mail.com"))

  override def receive = {
    case CheckUser(user) if blackList.contains(user) =>
      println(s"Checker: $user in the blacklist")
      sender() ! BlackUser(user)
    case CheckUser(user) =>
      println(s"Checker: $user not in the blacklist")
      sender() ! WhiteUser(user)
  }
}

class Recorder(checker: ActorRef, storage: ActorRef) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  override def receive = {
    case NewUser(user) =>
      checker ? CheckUser(user) map {
        case WhiteUser(user) =>
          storage ! AddUser(user)
        case BlackUser(user) =>
          println(s"Recorder: $user in the blacklist")
      }
  }
}

object TalkToActor extends App {

  //create the 'talk-to-actor' actor system
  val system = ActorSystem("talk-to-actor")

  //Create the 'checker' actor
  val checker = system.actorOf(Props[Checker], "checker")

  //Create the 'storage' actor
  val storage = system.actorOf(Props[Storage], "storage")

  //Create the 'recorder' actor
  val recorder = system.actorOf(Recorder.props(checker, storage), "recorder")

  //send NewUser Message to Recorder
  recorder ! Recorder.NewUser(User("Jon", "jon@packt.com"))

  Thread.sleep(100)

  //shutdown system
  system.terminate()

}