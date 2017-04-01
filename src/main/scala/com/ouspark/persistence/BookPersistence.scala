package com.ouspark.persistence

import com.ouspark.model.BookActor.{Book, Publisher}
import slick.driver.H2Driver.api._
/**
  * Created by spark.ou on 4/1/2017.
  */
class BookPersistence {

  import com.ouspark.persistence.Books._
  import com.ouspark.persistence.Publishers._

  lazy val db = Database.forConfig("db")

  def createSchema() = db.run(DBIO.seq((books.schema ++ publishers.schema).create))
  def createDataset() = db.run(DBIO.seq(
    publishers ++= Seq(
      Publisher(Some(1), "Packt Publishing"),
      Publisher(Some(2), "Manning Publications")
    ),

    books ++= Seq(
      Book("978-1783281411", "Learning Concurrent Programming in Scala", "Aleksandar Prokopec", 1),
      Book("978-1783283637", "Scala for Java Developers", "Thomas Alexandre", 1),
      Book("978-1935182757", "Scala in Action", "Nilanjan Raychaudhuri", 2)
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

}
