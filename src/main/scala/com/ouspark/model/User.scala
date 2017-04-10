package com.ouspark.model

import akka.actor.{Actor, Props}
import akka.pattern.pipe
import akka.http.scaladsl.server.directives.Credentials

import scala.concurrent.Future
import com.ouspark.model.UserActor.{Authenticate, GetUser}
import com.ouspark.persistence.BookPersistence

/**
  * Created by spark.ou on 4/7/2017.
  */
object Permissions extends Enumeration {
  type permission = Value
  val MANAGE_PUBLISHERS = Value("MANAGE_PUBLISHERS")
  val MANAGE_USERS = Value("MANAGE_USERS")
}

case class User(username: String, password: String, salt: Array[Byte], permissions: Seq[Permissions.permission] = Seq()) {
  def this(username: String, saltedPassword: (String, Array[Byte])) {
    this(username, saltedPassword._1, saltedPassword._2)
  }
}

class UserActor extends Actor {
  import scala.concurrent.ExecutionContext.Implicits.global
  val persistence = new BookPersistence

  def receive = {
    case Authenticate(credentials) => Future.successful(Some(credentials)) pipeTo sender()
    case GetUser(username) => persistence.findUserByUsername(username) pipeTo sender()

  }
}

object UserActor {
  def props = Props(new UserActor)
  def name = "UserActor"

  case class Authenticate(credentials: Credentials)
  case class CreateUser(username: String, password: String)
  case class GetUser(username: String)
}
