package controllers

import javax.inject._

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import play.api.libs.streams.ActorFlow
import play.api.mvc._


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer)
  extends AbstractController(cc) {

  var counter = 0

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    counter += 1
    Ok(views.html.index(counter))
  }

  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow actorRef MyWebSocketActor.props
  }

  def reset() = Action{
    counter = 0

    Redirect("/").flashing("message" -> "Reset")
  }
}


object MyWebSocketActor {
  def props(out: ActorRef) = Props(new MyWebSocketActor(out))
}

class MyWebSocketActor(out: ActorRef) extends Actor {

  def receive = {
    case msg: String => out ! ("I received your message: " + msg)
  }
}

