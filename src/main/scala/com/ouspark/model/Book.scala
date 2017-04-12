package com.ouspark.model

import akka.actor.{Actor, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.ouspark.model.BookActor._
import com.ouspark.model.PublisherActor.Publisher
import com.ouspark.persistence.BookPersistence
import org.joda.time.LocalDate

import scala.concurrent.Future


/**
  * Created by spark.ou on 4/1/2017.
  */

object BookActor {

  def props(implicit timeout: Timeout) = Props(new BookActor)
  def name = "bookActor"

  case class Book(isbn: String, title: String, author: String, publishDate: LocalDate, publisherId: Long)

  case class BookUpdatePayload(title: String, author: String, publishDate: LocalDate)
  case class BookCreatePayload(isbn: String, title: String, author: String, publishDate: LocalDate)

  case class BookResource(isbn: String, title: String, author: String, publishDate: LocalDate, publisher: Publisher)
  object BookResource {
    def apply(book: Book, publisher: Publisher) : BookResource =
      BookResource(book.isbn, book.title, book.author, book.publishDate, publisher)

  }

  case object GetBooks
  case class GetBook(isbn: String)
  case class UpdateBook(isbn: String, title: String, author: String, publishDate: LocalDate)
  case class DeleteBook(isbn: String)

  case class BookVolumeInfo(
                             title: String,
                             publisher: Option[String],
                             publishedDate: Option[String],
                             authors: Option[List[String]],
                             description: Option[String],
                             pageCount: Option[Int],
                             language: Option[String]
                           )
  case class BookSearchResultItem(volumeInfo: BookVolumeInfo)
  case class BookSearchResult(totalItems: Int, items: List[BookSearchResultItem])

}
class BookActor(implicit timeout: Timeout) extends Actor {
  import scala.concurrent.ExecutionContext.Implicits.global
  val persistence = new BookPersistence
  def receive = {
    case GetBooks =>
      val result = persistence.findAllBooks() map {
        _ map { case (book, publisher) => BookResource(book, publisher) }
      }
      result pipeTo sender()
    case GetBook(isbn) =>
      val result = persistence.findBookByIsbn(isbn) map {
        case Some((book, publisher)) => Some(BookResource(book, publisher))
        case _ => None
      }
      result pipeTo sender()
    case UpdateBook(isbn, title, author, publishDate) =>
      val result = persistence.updateBookByIsbn(isbn, title, author, publishDate) flatMap {
        case true => self ask GetBook(isbn)
        case _ => Future.successful(None)
      }
      result pipeTo sender()
    case DeleteBook(isbn) =>
      val result = persistence.deleteBookByIsbn(isbn)
      result pipeTo sender()
  }
}
