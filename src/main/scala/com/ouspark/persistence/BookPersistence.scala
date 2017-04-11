package com.ouspark.persistence

import com.ouspark.model.BookActor.Book
import com.ouspark.model.PublisherActor.Publisher
import org.joda.time.LocalDate
import com.github.tototoshi.slick.H2JodaSupport._
import com.ouspark.model.{Permission, Permissions, User}
import slick.driver.H2Driver.api._

import scala.language.postfixOps
/**
  * Created by spark.ou on 4/1/2017.
  */
class BookPersistence {

  import com.ouspark.persistence.Books._
  import com.ouspark.persistence.Publishers._
  import com.ouspark.persistence.Users._
  import com.ouspark.persistence.UserPermissions._
  import com.ouspark.security.PasswordHasher._
  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val db = Database.forConfig("db")

  def createSchema() = db.run(DBIO.seq((books.schema ++ publishers.schema ++ users.schema ++ permissions.schema).create))
  def createDataset() = db.run(DBIO.seq(
    publishers ++= Seq(
      Publisher(Some(1), "Packt Publishing"),
      Publisher(Some(2), "Manning Publications")
    ),

    books ++= Seq(
      Book("978-1783281411", "Learning Concurrent Programming in Scala", "Aleksandar Prokopec", new LocalDate(2014, 11, 25), 1),
      Book("978-1783283637", "Scala for Java Developers", "Thomas Alexandre", new LocalDate(2014, 6, 11), 1),
      Book("978-1935182757", "Scala in Action", "Nilanjan Raychaudhuri", new LocalDate(2013, 4, 13), 2)
    ),

    users ++= Seq(
      new User("admin", hash("passw0rd")),
      new User("librarian", hash("passw0rd")),
      new User("user", hash("passw0rd"))
    ),

    permissions ++= Seq(
//      Permission("admin", Permissions.MANAGE_BOOKS),
      Permission("admin", Permissions.MANAGE_PUBLISHERS),
      Permission("librarian", Permissions.MANAGE_BOOKS)
    )
  ))


  def findAllBooks() = {
    val query = for((book, publisher) <- books join publishers on (_.publisherId === _.id)) yield (book, publisher)
    db.run(query.result)
  }

  def findBookByIsbn(isbn: String) = {
    val query = for((book, publisher) <- books.filter(_.isbn === isbn) join publishers on (_.publisherId === _.id)) yield (book, publisher)
    db.run(query.result.headOption)
  }

  def updateBookByIsbn(isbn: String, title: String, author: String, publishDate: LocalDate) = {
    val query = for(book <- books.filter(_.isbn === isbn)) yield (book.title, book.author, book.publishDate)
    db.run(query.update(title, author, publishDate)) map { _ > 0 }
  }

  def deleteBookByIsbn(isbn: String) = {
    val query = for(book <- books.filter(_.isbn === isbn)) yield book
    db.run(query.delete) map { _ > 0 }
  }

  def findAllPublishers() = {
    db.run(publishers.result)
  }

  def findPublisherById(id: Long) = {
    val query = for(publisher <- publishers.filter(_.id === id)) yield publisher
    db.run(query.result.headOption)
  }

  def updatePublisherById(id: Long, name: String) = {
    val query = for(publisher <- publishers.filter(_.id === id)) yield (publisher.id, publisher.name)
    db.run(query.update(id, name)) map { _ > 0 }
  }

  def deletePublisherById(id: Long) = {
    val query = for(publisher <- publishers.filter(_.id === id)) yield publisher
    db.run(query.delete) map { _ > 0 }
  }

  def addPublisher(name: String) = {
    val query = publishers returning publishers.map(_.id) into ((publisher, id) => publisher.copy(id = Some(id)))
    db.run(query += Publisher(None, name))
  }

  def addBook(book: Book) = {
    db.run(books += book)
  }

  def findUserByUsername(username: String) = {
    val query = for(user <- users.filter(_.username === username)) yield user
    db.run(query.result.headOption)
//    val query = for {
//      (u, p) <- users.filter(_.username === username).joinLeft(userPermissions).on(_.username === _.username)
//    } yield (u, p.map(_.permission))
//
//    db.run(query.result) map { result =>
//      result groupBy (_._1) map { case (key, value) =>
//        key.copy(permissions = result.filter(_._1.username == key.username).map(_._2).flatten)
//      } headOption
//    }
  }

  def checkPermission(user: User, permission: Permissions.Permission) = {
    val query = for(permission <- permissions.filter(_.username === user.username).filter(_.permission === permission)) yield permission
    db.run(query.result.headOption) map { !_.isEmpty }
  }

}
