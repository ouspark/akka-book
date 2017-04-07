package com.ouspark.model


import akka.actor.{Actor, Props}
import akka.pattern.pipe
import akka.pattern.ask
import akka.util.Timeout
import com.ouspark.model.BookActor.{Book, BookResource}
import com.ouspark.model.PublisherActor._
import com.ouspark.persistence.BookPersistence

import scala.concurrent.Future

/**
  * Created by spark.ou on 4/6/2017.
  */
class PublisherActor(implicit timeout: Timeout) extends Actor {
  import scala.concurrent.ExecutionContext.Implicits.global

  val persistence = new BookPersistence

  def receive = {
    case GetPublishers =>
      persistence.findAllPublishers() pipeTo sender()
    case GetPublisher(id) =>
      persistence.findPublisherById(id) pipeTo sender()
    case UpdatePublisher(id, name) =>
      persistence.updatePublisherById(id, name) flatMap {
        case true => self ask GetPublisher(id)
        case _ => Future.successful(None)
      }
    case DeletePublisher(id) =>
      persistence.deletePublisherById(id) pipeTo sender()
    case AddPublisher(name) =>
      persistence.addPublisher(name) pipeTo sender()
    case AddBook(book) =>
      val result = persistence.findPublisherById(book.publisherId) flatMap {
        case Some(publisher) => persistence.addBook(book) map { book1 => Some(BookResource(book, publisher)) }
        case _ => Future.successful(None)
      }
      result pipeTo sender()
  }
}

object PublisherActor {
  def props(implicit timeout: Timeout) = Props(new PublisherActor)
  def name = "publisherActor"

  case class Publisher(id: Option[Long], name: String)

  case object GetPublishers
  case class GetPublisher(id: Long)
  case class UpdatePublisher(id: Long, name: String)
  case class DeletePublisher(id: Long)
  case class AddPublisher(name: String)
  case class AddBook(book: Book)
}