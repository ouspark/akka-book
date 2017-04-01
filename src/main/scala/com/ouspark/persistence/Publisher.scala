package com.ouspark.persistence

import com.ouspark.model.BookActor.Publisher
import slick.driver.H2Driver.api._

/**
  * Created by spark.ou on 4/1/2017.
  */
class Publishers(tag: Tag) extends Table[Publisher](tag, "PUBLISHERS") {

  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME", O.Length(256))

  def uniqueName = index("NAME_IDX", name, true)
  def * = (id.?, name) <> (Publisher.tupled, Publisher.unapply)

}

object Publishers {
  val publishers = TableQuery[Publishers]
}