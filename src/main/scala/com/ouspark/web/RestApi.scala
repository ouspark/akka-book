package com.ouspark.web

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import com.ouspark.model.BookActor
import com.ouspark.model.BookActor._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by spark.ou on 4/1/2017.
  */
class RestApi(actorSystem: ActorSystem, timeout: Timeout) extends BookRestRoute {
  implicit val requestTimeout = timeout
  implicit def executionContext = actorSystem.dispatcher

  def createBookActor = actorSystem.actorOf(BookActor.props, BookActor.name)

}

trait BookRestRoute extends BookRestApi with EventMarshalling {
  import akka.http.scaladsl.model.StatusCodes._

  val routes = booksRoute ~ bookRoute

  def booksRoute = pathPrefix("books") {
    pathEndOrSingleSlash {
      pathEndOrSingleSlash {
        get {
          onSuccess(getBooks()) { result =>
            complete (OK, result)
          }
        }
      }
    }
  }

  def bookRoute = pathPrefix("books" / Segment) { isbn =>
    pathEndOrSingleSlash {
      get {
        complete(getBook(isbn))
//        onSuccess(getBook(isbn)) {
//          _.fold(complete(NotFound))(e => complete(OK, e))
//        }
      }
    }
  }

}

trait BookRestApi {
  def createBookActor(): ActorRef
  implicit def executionContext: ExecutionContext
  implicit val requestTimeout: Timeout
  lazy val bookActor = createBookActor()
  def getBooks() = bookActor.ask(GetBooks).mapTo[Future[Seq[(Book, Publisher)]]]
  def getBook(isbn: String) = bookActor.ask(GetBook(isbn)).mapTo[Future[Option[(Book, Publisher)]]]
}


trait PublisherRestRoute {


}