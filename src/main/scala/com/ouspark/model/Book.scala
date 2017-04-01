package com.ouspark.model

import akka.actor.{Actor, Props}
import akka.util.Timeout
import com.ouspark.model.BookActor._
import com.ouspark.persistence.BookPersistence

/**
  * Created by spark.ou on 4/1/2017.
  */

case object BookActor {

  case class Book(isbn: String, title: String, author: String, publisherId: Long)
  case class Publisher(id: Option[Long], name: String)

  case class BookResource(isbn: String, title: String, author: String, publisher: Publisher)
  object BookResource {
    def apply(book: Book, publisher: Publisher) : BookResource =
      BookResource(book.isbn, book.title, book.author, publisher)

  }
  def props(implicit timeout: Timeout) = Props(new BookActor)
  def name = "bookActor"
  case object GetBooks
  case class GetBook(isbn: String)

}
class BookActor(implicit timeout: Timeout) extends Actor {
  val persistence = new BookPersistence
  def receive = {
    case GetBooks =>
      sender() ! persistence.findAllBooks()
    case GetBook(isbn) =>
      sender() ! persistence.findBookByIsbn(isbn)
  }
}
