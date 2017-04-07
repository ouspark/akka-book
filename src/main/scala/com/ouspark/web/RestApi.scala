package com.ouspark.web

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import com.ouspark.model.BookActor._
import com.ouspark.model.PublisherActor._
import com.ouspark.model.{BookActor, PublisherActor}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import org.joda.time.LocalDate

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Created by spark.ou on 4/1/2017.
  */
class RestApi(actorSystem: ActorSystem, timeout: Timeout) extends BookRestRoute {
  implicit val requestTimeout = timeout
  implicit def executionContext = actorSystem.dispatcher

  def createBookActor = actorSystem.actorOf(BookActor.props, BookActor.name)
  def createPublisherActor = actorSystem.actorOf(PublisherActor.props, PublisherActor.name)

}

trait BookRestRoute extends BookRestApi with PublisherRestRoute {

  val routes = pathPrefix("api" / "v1") {
    booksRoute ~ bookRoute ~ publishersRoute ~ publisherRoute
  }

}

trait BookRestApi extends EventMarshalling {

  def createBookActor(): ActorRef
  implicit def executionContext: ExecutionContext
  implicit val requestTimeout: Timeout
  lazy val bookActor = createBookActor()

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

  def bookRoute = pathPrefix("books" / """\d{3}[-]\d{10}""".r) { isbn =>
    pathEndOrSingleSlash {
      get {
        onSuccess(getBook(isbn)) {
          _.fold(complete(NotFound))(e => complete(OK, e))
        }
      } ~
        put {
          entity(as[BookUpdatePayload]) { payload =>
            rejectEmptyResponse {
              onSuccess(updateBook(isbn, payload.title, payload.author, payload.publishDate)) {
                _.fold(complete(NotFound))(e => complete(OK, e))
              }
            }
          }
        } ~
        delete {
          complete {
            deleteBook(isbn) map {
              case true => NoContent
              case _ => NotFound
            }
          }
        }
    }
  }


  def getBooks() = bookActor.ask(GetBooks).mapTo[Seq[BookResource]]
  def getBook(isbn: String) = bookActor.ask(GetBook(isbn)).mapTo[Option[BookResource]]
  def updateBook(isbn: String, title: String, author: String, publishDate: LocalDate) = bookActor.ask(UpdateBook(isbn, title, author, publishDate)).mapTo[Option[BookResource]]
  def deleteBook(isbn: String) = bookActor.ask(DeleteBook(isbn)).mapTo[Boolean]
}


trait PublisherRestRoute extends EventMarshalling {
  def createPublisherActor(): ActorRef
  implicit def executionContext: ExecutionContext
  implicit val requestTimeout: Timeout

  lazy val publisherActor = createPublisherActor()

  def publishersRoute = pathPrefix("publishers") {
    pathEndOrSingleSlash {
      get {
        onSuccess(getPublishers) { result =>
          complete(OK, result)
        }
      } ~
      post {
        rejectEmptyResponse {
          entity(as[Publisher]) { payload =>
            onComplete(addPublisher(payload.name)) {
              case Success(publisher) => complete(Created, publisher)
              case Failure(ex) => complete(Conflict)
            }
          }
        }
      }
    }
  }

  def publisherRoute = pathPrefix("publishers" / LongNumber) { id =>
    pathEndOrSingleSlash {
      get {
        onSuccess(getPublisher(id)) {
          _.fold(complete(NotFound))(e => complete(OK, e))
        }
      } ~
      put {
        entity(as[Publisher]) { payload =>
          rejectEmptyResponse {
            onSuccess(updatePublisher(id, payload.name)) {
              _.fold(complete(NotFound))(e => complete(OK, e))
            }
          }
        }
      } ~
      delete {
        complete {
          deletePublisher(id) map {
            case true => NoContent
            case _ => NotFound
          }
        }
      } ~
      post {
        rejectEmptyResponse {
          entity(as[BookCreatePayload]) { payload =>
            val newBook = Book(payload.isbn, payload.title, payload.author, payload.publishDate, id)
            onComplete(addBook(newBook)) {
              case Success(Some(bookResource)) => complete(Created, bookResource)
              case Success(_) => complete(NotFound)
              case Failure(ex) => complete(Conflict)
            }
          }
        }
      }
    }
  }

  def getPublishers() = publisherActor.ask(GetPublishers).mapTo[Seq[Publisher]]
  def getPublisher(id: Long) = publisherActor.ask(GetPublisher(id)).mapTo[Option[Publisher]]
  def updatePublisher(id: Long, name: String) = publisherActor.ask(UpdatePublisher(id, name)).mapTo[Option[Publisher]]
  def deletePublisher(id: Long) = publisherActor.ask(DeletePublisher(id)).mapTo[Boolean]
  def addPublisher(name: String) = publisherActor.ask(AddPublisher(name)).mapTo[Publisher]
  def addBook(book: Book) = publisherActor.ask(AddBook(book)).mapTo[Option[BookResource]]
}