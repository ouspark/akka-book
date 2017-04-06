package com.ouspark.persistence

import org.joda.time.LocalDate
import com.github.tototoshi.slick.H2JodaSupport._
import com.ouspark.model.BookActor.Book
import slick.driver.H2Driver.api._
import com.ouspark.persistence.Publishers._

/**
  * Created by spark.ou on 4/1/2017.
  */
class Books(tag: Tag) extends Table[Book](tag, "BOOKS") {
  def isbn = column[String]("ISBN", O.PrimaryKey, O.Length(14))
  def title = column[String]("TITLE", O.Length(512))
  def author = column[String]("AUTHOR", O.Length(256))
  def publishDate = column[LocalDate]("PUBLISH_DATE")
  def publisherId = column[Long]("PUBLISHER_ID")

  def publisher = foreignKey("PUBLISHER_FK", publisherId, publishers)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def * = (isbn, title, author, publishDate, publisherId) <> (Book.tupled, Book.unapply)

}

object Books {
  val books = TableQuery[Books]
}
