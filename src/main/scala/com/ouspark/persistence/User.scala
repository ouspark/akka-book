package com.ouspark.persistence
import com.ouspark.model.User
import slick.driver.H2Driver.api._

/**
  * Created by spark.ou on 4/10/2017.
  */
class Users(tag: Tag) extends Table[User](tag, "USERS") {
  def username = column[String]("USERNAME", O.PrimaryKey, O.Length(128))
  def password = column[Array[Byte]]("PASSWORD", O.Length(128))
  def salt = column[Array[Byte]]("SALT", O.Length(32))

  def * = (username, password, salt).shaped <> (
    { tuple => User(tuple._1, tuple._2, tuple._3) },
    { user: User => Some((user.username, user.password, user.salt)) }
  )
}

object Users {
  val users = TableQuery[Users]
}